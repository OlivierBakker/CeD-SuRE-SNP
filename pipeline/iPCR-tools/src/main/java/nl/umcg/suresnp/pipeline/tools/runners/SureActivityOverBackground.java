package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.IpcrTools;
import nl.umcg.suresnp.pipeline.io.bedreader.GenericGenomicAnnotationReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BlockCompressedIpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.MultiFileBlockCompressedIpcrFileReader;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.SampleSumScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.tools.parameters.SureActivityOverBackgroundParameters;
import nl.umcg.suresnp.pipeline.utils.B37GenomeInfo;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static nl.umcg.suresnp.pipeline.tools.runners.GenomicRegionEnrichment.generatePermutedGenomicRegionsMatchingQuery;
import static nl.umcg.suresnp.pipeline.tools.runners.GenomicRegionEnrichment.intersectTreeMaps;

public class SureActivityOverBackground {

    private static final Logger LOGGER = Logger.getLogger(SureActivityOverBackground.class);
    private SureActivityOverBackgroundParameters params;
    private int nPerm;

    public SureActivityOverBackground(SureActivityOverBackgroundParameters params) throws IOException {
        this.params = params;

        // Not needed, but makes code a bit cleaner
        this.nPerm = params.getNumberOfPermutations();
    }

    public void run() throws IOException {

        // Set of regions used to determine true overlap (i.e. SuRE peaks)
        IntervalTreeMap<BedRecord> querySet = new GenericGenomicAnnotationReader(params.getQueryRegions(), false, params.isTrimChrFromContig()).getBedRecordsAsTreeMap();

        // Set of regions to restrict the analysis to. Can be genome wide.
        IntervalTreeMap<BedRecord> targetRegions = new GenericGenomicAnnotationReader(params.getTargetRegions(), false, params.isTrimChrFromContig()).getBedRecordsAsTreeMap();

        // Filter sets on target regions
        querySet = intersectTreeMaps(querySet.values(), targetRegions);

        LOGGER.info("Retained " + querySet.size() + " query peaks");

        // Average SuRE-activity in query set
        MultiFileBlockCompressedIpcrFileReader reader = new MultiFileBlockCompressedIpcrFileReader(params.getInputIpcr());
        AdaptableScoreProvider summingScoreProvider = new SampleSumScoreProvider(params.getSamplesToWrite());

        double trueSureActivity = getAverageSureActivityOverPeaks(querySet.values(), summingScoreProvider, reader);
        LOGGER.info("True activity: " + trueSureActivity);

        // SuRE activity in random regions
        // Cat to chr map
        final Map<String, ArrayList<BedRecord>> regionsToSampleFromPerChr = new HashMap<>();
        for (String contig : B37GenomeInfo.getChromosomes()) {
            Collection<BedRecord> curSet = targetRegions.getOverlapping(new Interval(contig, 0, Integer.MAX_VALUE - 10));
            regionsToSampleFromPerChr.put(contig, new ArrayList<>(curSet));
        }

        double[] permutedSureActivity = new double[nPerm];
        double meanPermutedSureActivity = 0;

        LOGGER.info("Running " + nPerm + " permutations");
        for (int i=0; i < nPerm; i++) {
            List<BedRecord> randomSurePeaks = generatePermutedGenomicRegionsMatchingQuery(querySet.values(), regionsToSampleFromPerChr);
            permutedSureActivity[i] = getAverageSureActivityOverPeaks(randomSurePeaks, summingScoreProvider, reader);
            meanPermutedSureActivity += permutedSureActivity[i];

            IpcrTools.logProgress(i, 1, "SureActivityOverBackground", "");
        }

        meanPermutedSureActivity = meanPermutedSureActivity / nPerm;

        LOGGER.info("Mean permuted activity: " + meanPermutedSureActivity);

        // Emperical p-value & relative enrichment

        // Determine rank of true value in the permuted distribution
        Arrays.sort(permutedSureActivity);
        int finalRank = 0;
        for (int i = 0; i < permutedSureActivity.length; i++) {
            if (trueSureActivity > permutedSureActivity[i]) {
                finalRank++;
            }
        }

        // Determine the relative enrichment over the mean in the null
        double relativeEnrichment = trueSureActivity / meanPermutedSureActivity;

        // If there is depletion, adjust the rank so the pvalue calculation is valid
        if (relativeEnrichment < 1) {
            finalRank = permutedSureActivity.length - finalRank;
        }

        // Determine the two-sided emperical pvalue
        double empericalPvalue = (1 - ((finalRank + 0.5) / (permutedSureActivity.length + 1))) *2;

        LOGGER.info("Ratio: " + relativeEnrichment);
        LOGGER.info("P-value: " + empericalPvalue);

    }

    private double getAverageSureActivityOverPeaks(Collection<BedRecord> queryRecords, AdaptableScoreProvider provider, MultiFileBlockCompressedIpcrFileReader reader) throws IOException {

        double peakMean = 0;
        int i = 0;

        //LOGGER.debug("Number of query records: " + queryRecords.size());

        for (BedRecord curPeak: queryRecords) {

            double cdnaCount = 0;
            double ipcrCount = 0;

            List<IpcrRecord> curIpcrRecords =reader.queryAsList(curPeak.getContig(), curPeak.getStart(), curPeak.getEnd());
            //LOGGER.debug("Number of overlapping iPCR records: " + curIpcrRecords.size());

            if (curIpcrRecords.size() == 0) {
                continue;
            }

            for (IpcrRecord curRecord : curIpcrRecords) {
                cdnaCount += provider.getScore(curRecord);
                ipcrCount += curRecord.getIpcrDuplicateCount();
            }

            peakMean += cdnaCount / ipcrCount;
            i++;
            ///LOGGER.debug("cdnaCount: " + cdnaCount);
            //LOGGER.debug("ipcrCount: " + ipcrCount);

            //LOGGER.debug("peakMean: " + peakMean);

        }

        return(peakMean / i);
    }


}
