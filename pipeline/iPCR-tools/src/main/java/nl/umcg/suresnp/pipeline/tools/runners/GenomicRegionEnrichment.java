package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.IpcrTools;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ReferenceBedFileType;
import nl.umcg.suresnp.pipeline.io.bedreader.GenericGenomicAnnotationReader;
import nl.umcg.suresnp.pipeline.io.bedwriter.FourColBedWriter;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.GenomicRegionEnrichmentParameters;
import nl.umcg.suresnp.pipeline.utils.B37GenomeInfo;
import nl.umcg.suresnp.pipeline.utils.FisherExactTest;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GenomicRegionEnrichment {

    private static final Logger LOGGER = Logger.getLogger(GenomicRegionEnrichment.class);
    private GenomicRegionEnrichmentParameters params;
    private int nPerm;
    private FourColBedWriter debugRandomRecordWriter;

    public GenomicRegionEnrichment(GenomicRegionEnrichmentParameters params) throws IOException {
        this.params = params;
        this.nPerm = params.getNumberOfPermutations();
        this.debugRandomRecordWriter = new FourColBedWriter(new File(params.getOutputPrefix() + "_debug_null_records.bed"), true);
    }

    public void run() throws IOException {
        // Set of regions used to determine true overlap
        IntervalTreeMap<BedRecord> querySet = new GenericGenomicAnnotationReader(params.getQuery(), false, params.isTrimChrFromContig()).getBedRecordsAsTreeMap();

        // Set of regions used to overlap the query set with
        Map<String, IntervalTreeMap<BedRecord>> referenceSet = readReferenceData();

        // Set of regions to restrict the analysis to. Can be genome wide.
        IntervalTreeMap<BedRecord> targetRegions = new GenericGenomicAnnotationReader(params.getTargetRegions(), false, params.isTrimChrFromContig()).getBedRecordsAsTreeMap();

        // Filter sets on target regions
        querySet = intersectTreeMaps(querySet.values(), targetRegions);

        for (String database : referenceSet.keySet()) {
            IntervalTreeMap<BedRecord> filteredSet = intersectTreeMaps(referenceSet.get(database).values(), targetRegions);
            referenceSet.replace(database, filteredSet);
        }

        // Determine true overlaps
        Map<String, int[]> trueOverlaps = new HashMap<>();

        for (String database : referenceSet.keySet()) {
            trueOverlaps.put(database, determineOverlap(querySet.values(), referenceSet.get(database)));
        }

        // Determine permuted overlaps
        Map<String, int[][]> permutedOverlaps = new HashMap<>();

        // Cat to chr map
        final Map<String, ArrayList<BedRecord>> regionsToSampleFromPerChr = new HashMap<>();
        for (String contig : B37GenomeInfo.getChromosomes()) {
            Collection<BedRecord> curSet = targetRegions.getOverlapping(new Interval(contig, 0, Integer.MAX_VALUE - 10));
            regionsToSampleFromPerChr.put(contig, new ArrayList<>(curSet));
        }

        for (int i = 0; i < nPerm; i++) {
            List<BedRecord> currentRandom = generatePermutedGenomicRegionsMatchingQuery(querySet.values(), regionsToSampleFromPerChr);
            IpcrTools.logProgress(i, 1000, "GenomicRegionEnrichment", "thousand");

            for (String database : referenceSet.keySet()) {
                int[][] currentPermutedOverlaps;
                if (!permutedOverlaps.containsKey(database)) {
                    currentPermutedOverlaps = new int[nPerm][];
                    currentPermutedOverlaps[i] = determineOverlap(currentRandom, referenceSet.get(database));
                    permutedOverlaps.put(database, currentPermutedOverlaps);
                } else {
                    permutedOverlaps.get(database)[i] = determineOverlap(currentRandom, referenceSet.get(database));
                }
            }
        }

        // Initialize the output writer
        BufferedWriter mainOutputWriter = new GenericFile(params.getOutputPrefix() + ".enrichment.results").getAsBufferedWriter();
        mainOutputWriter.write("database\temperical_pvalue\tpercentage_overlap\trelative_enrichment\toverlapping\tnon_overlapping\tpermuted_mean\tpermuted_sd");
        mainOutputWriter.newLine();

        // Determine emperical pvalues for the enrichment in each reference set
        for (String database : referenceSet.keySet()) {

            // Determine the true ratio of overlaps
            int[] curTrueOverlaps = trueOverlaps.get(database);
            double trueRatio = (double) curTrueOverlaps[0] / (curTrueOverlaps[0] + curTrueOverlaps[1]);

            // Determine the ratio of overlaps in the permuted sets
            int[][] curPermutedOverlaps = permutedOverlaps.get(database);
            double[] permutedRatios = new double[curPermutedOverlaps.length];
            for (int i = 0; i < curPermutedOverlaps.length; i++) {
                permutedRatios[i] = (double) curPermutedOverlaps[i][0] / (curPermutedOverlaps[i][0] + curPermutedOverlaps[i][1]);
            }

            // Determine rank of true value in the permuted distribution
            Arrays.sort(permutedRatios);
            int finalRank = 0;
            for (int i = 0; i < permutedRatios.length; i++) {
                if (trueRatio > permutedRatios[i]) {
                    finalRank++;
                }
            }

            // Determine mean and SD of random dist
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(permutedRatios);

            // Determine the relative enrichment over the mean in the null
            double relativeEnrichment = trueRatio / descriptiveStatistics.getMean();

            // If there is depletion, adjust the rank so the pvalue calculation is valid
            if (relativeEnrichment < 1) {
                finalRank = permutedRatios.length - finalRank;
            }

            // Determine the two-sided emperical pvalue
            double empericalPvalue = (1 - ((finalRank + 0.5) / (permutedRatios.length + 1))) *2;

            // Determine the pseudo emperical pvalue based on a normal dist
            // TODO: maybe do this on a poisson of the overlapped counts instead
            /*double pseudoEmpericalPvalue = 1;
            if (descriptiveStatistics.getStandardDeviation() > 0) {
                NormalDistribution empericalNormalDist = new NormalDistribution(descriptiveStatistics.getMean(), descriptiveStatistics.getStandardDeviation());

                if (relativeEnrichment > 1) {
                    pseudoEmpericalPvalue = 1 - empericalNormalDist.cumulativeProbability(trueRatio);
                } else {
                    pseudoEmpericalPvalue = empericalNormalDist.cumulativeProbability(trueRatio);
                }
            }*/

            // If overlap is zero report pval of 1 to avoid confusion in the results
            if (trueRatio == 0) {
                empericalPvalue = 1;
            }

            // Write the summary statistics
            mainOutputWriter.write(database);
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Double.toString(empericalPvalue));
            //mainOutputWriter.write("\t");
            //mainOutputWriter.write(Double.toString(pseudoEmpericalPvalue));
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Double.toString(trueRatio));
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Double.toString(relativeEnrichment));
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Integer.toString(curTrueOverlaps[0]));
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Integer.toString(curTrueOverlaps[1]));
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Double.toString(descriptiveStatistics.getMean()));
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Double.toString(descriptiveStatistics.getStandardDeviation()));
            mainOutputWriter.newLine();
            //LOGGER.info("Pvalue for " + database + ": " + empericalPvalue);

        }

        // Flush and close the output writer
        mainOutputWriter.flush();
        mainOutputWriter.close();

        // Save the null distributions
        writeNullDistributionsAsMatrix(referenceSet, permutedOverlaps);

        debugRandomRecordWriter.flushAndClose();
        LOGGER.info("done");

    }

    /**
     * Simulated a set of random genomic regions matching the query in number, size and chromosomal distribution
     *
     * @param query
     * @param regionsToSampleFrom
     * @return
     */
    private List<BedRecord> generatePermutedGenomicRegionsMatchingQuery(Collection<BedRecord> query, Map<String, ArrayList<BedRecord>> regionsToSampleFrom) throws IOException {
        List<BedRecord> output = new ArrayList<>(query.size());

        for (BedRecord currentQuery : query) {
            int size = currentQuery.getEnd() - currentQuery.getStart();

            // Determine the available regions to sample from and select one randomly on the same chromosome as query
            ArrayList<BedRecord> possibleRegions = regionsToSampleFrom.get(currentQuery.getContig());
            int indexToSample = ThreadLocalRandom.current().nextInt(0, possibleRegions.size());
            BedRecord regionToSampleIn = possibleRegions.get(indexToSample);

            int newRegionMidPoint = ThreadLocalRandom.current().nextInt(regionToSampleIn.getStart(), regionToSampleIn.getEnd());
            BedRecord tmp = new BedRecord(currentQuery.getContig(), newRegionMidPoint - (size / 2), newRegionMidPoint + (size / 2));
            output.add(tmp);
            debugRandomRecordWriter.writeRecord(tmp);
        }

        return output;
    }

    private int[] determineOverlap(Collection<BedRecord> query, IntervalTreeMap<BedRecord> reference) {
        int[] output = new int[2];

        for (BedRecord curRecord : query) {
            if (reference.containsOverlapping(curRecord)) {
                output[0]++;
            }
        }

        output[1] = query.size() - output[0];
        return output;
    }

    private IntervalTreeMap<BedRecord> intersectTreeMaps(Collection<BedRecord> query, IntervalTreeMap<BedRecord> reference) {
        IntervalTreeMap<BedRecord> output = new IntervalTreeMap<>();

        int removed = 0;
        int i = 0;
        for (BedRecord curRec : query) {
            if (!reference.containsOverlapping(curRec)) {
                removed++;
            } else {
                output.put(curRec, curRec);
            }
            i++;

        }

        //LOGGER.info("Filtered " + removed + " records");
        return output;
    }

    private void writeNullDistributionsAsMatrix(Map<String, IntervalTreeMap<BedRecord>> referenceSet, Map<String, int[][]> permutedOverlaps) throws IOException {
        // Write the null dist as matrix format. This is done seperately so each collumn can be a database
        LOGGER.info("Writing null dist matrix");

        List<String> databaseOrder = new ArrayList<>(referenceSet.keySet());
        BufferedWriter nullDistOutputWriter = new GenericFile(params.getOutputPrefix() + ".enrichment.nulldist").getAsBufferedWriter();

        // Write header
        nullDistOutputWriter.write("permutation");

        for (String database : databaseOrder) {
            nullDistOutputWriter.write("\t");
            nullDistOutputWriter.write(database);
        }
        nullDistOutputWriter.newLine();

        // Write rows
        for (int i = 0; i < nPerm; i++) {
            nullDistOutputWriter.write(Integer.toString(i));
            for (String database : databaseOrder) {
                int[][] curPermutedOverlaps = permutedOverlaps.get(database);
                double curCol = (double) curPermutedOverlaps[i][0] / (curPermutedOverlaps[i][0] + curPermutedOverlaps[i][1]);
                nullDistOutputWriter.write("\t");
                nullDistOutputWriter.write(Double.toString(curCol));
            }

            nullDistOutputWriter.newLine();
        }

        nullDistOutputWriter.flush();
        nullDistOutputWriter.close();
    }

    private Map<String, IntervalTreeMap<BedRecord>> readReferenceData() throws IOException {
        Map<String, IntervalTreeMap<BedRecord>> referenceSet = new HashMap<>();

        if (params.getReferenceBedFileType().getBedFileType() == ReferenceBedFileType.BedFileType.THREE_COL) {
            for (String database : params.getReferenceFiles().keySet()) {
                GenericGenomicAnnotationReader reader = new GenericGenomicAnnotationReader(params.getReferenceFiles().get(database), false, params.isTrimChrFromContig());
                referenceSet.put(database, reader.getBedRecordsAsTreeMap());
            }
        } else if (params.getReferenceBedFileType().getBedFileType() == ReferenceBedFileType.BedFileType.FOUR_COL) {
            for (String database : params.getReferenceFiles().keySet()) {
                GenericGenomicAnnotationReader reader = new GenericGenomicAnnotationReader(params.getReferenceFiles().get(database), false, params.isTrimChrFromContig());

                int i=0;
                for (GenericGenomicAnnotationRecord curRec: reader) {
                    // Adjust the column number by 4, the first three are for the standard BED columns, the
                    // additional 1 is for the zero indexing
                    String type = curRec.getAnnotations().get(params.getReferenceBedFileType().getColumnToSplit() - 4);
                    String databaseName = database + "_" + type;
                    if (referenceSet.containsKey(databaseName)) {
                        referenceSet.get(databaseName).put(curRec, curRec);
                    } else {
                        IntervalTreeMap<BedRecord> curNewMap = new IntervalTreeMap<>();
                        curNewMap.put(curRec, curRec);
                        referenceSet.put(databaseName, curNewMap);
                    }
                    i++;
                }
                LOGGER.info("Read " + i + " records from file: " + params.getReferenceFiles().get(database).getFileName());
            }

        } else {
            throw new IllegalArgumentException("No valid ReferenceBedFileType provided");
        }


        return referenceSet;
    }
}
