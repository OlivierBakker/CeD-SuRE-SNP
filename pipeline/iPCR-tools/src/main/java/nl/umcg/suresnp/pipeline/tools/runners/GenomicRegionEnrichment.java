package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.IpcrTools;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.bedreader.GenericGenomicAnnotationReader;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.GenomicRegionEnrichmentParameters;
import nl.umcg.suresnp.pipeline.utils.B37GenomeInfo;
import nl.umcg.suresnp.pipeline.utils.FisherExactTest;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GenomicRegionEnrichment {

    private static final Logger LOGGER = Logger.getLogger(GenomicRegionEnrichment.class);
    private GenomicRegionEnrichmentParameters params;

    public GenomicRegionEnrichment(GenomicRegionEnrichmentParameters params) {
        this.params = params;

    }

    public void run() throws IOException {
        int nPerm = params.getNumberOfPermutations();

        IntervalTreeMap<BedRecord> querySet = new GenericGenomicAnnotationReader(params.getQuery(), false).getBedRecordsAsTreeMap();

        Map<String, IntervalTreeMap<BedRecord>> referenceSet = new HashMap<>();
        for (String database : params.getReferenceFiles().keySet()) {
            GenericGenomicAnnotationReader reader = new GenericGenomicAnnotationReader(params.getReferenceFiles().get(database), false);
            referenceSet.put(database, reader.getBedRecordsAsTreeMap());
        }

        final IntervalTreeMap<BedRecord> targetSet = new GenericGenomicAnnotationReader(params.getTargetRegions(), false).getBedRecordsAsTreeMap();

        // Filter sets on target regions
        querySet = intersectTreeMaps(querySet.values(), targetSet);

        for (String database : referenceSet.keySet()) {
            IntervalTreeMap<BedRecord> filteredSet = intersectTreeMaps(referenceSet.get(database).values(), targetSet);
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
            Collection<BedRecord> curSet = targetSet.getOverlapping(new Interval(contig, 0, Integer.MAX_VALUE - 10));
            regionsToSampleFromPerChr.put(contig, new ArrayList<>(curSet));
        }

        for (int i = 0; i < nPerm; i++) {
            List<BedRecord> currentRandom = generatePermutedGenomicRegionsMatchingQuery(querySet.values(), regionsToSampleFromPerChr);
            IpcrTools.logProgress(i, 1000, "GenomicRegionEnrichment", "thousand");

            for (String database : referenceSet.keySet()) {
                int[][] currentPermutedOverlaps = permutedOverlaps.get(database);
                if (currentPermutedOverlaps == null) {
                    currentPermutedOverlaps = new int[nPerm][];
                    currentPermutedOverlaps[i] = determineOverlap(currentRandom, referenceSet.get(database));
                    permutedOverlaps.put(database, currentPermutedOverlaps);
                } else {
                    permutedOverlaps.get(database)[i] = determineOverlap(currentRandom, referenceSet.get(database));
                }
            }

        }

        BufferedWriter mainOutputWriter = new GenericFile(params.getOutputPrefix() + ".enrichment.results").getAsBufferedWriter();
        mainOutputWriter.write("database\temperical_pvalue\tpseudo_emperical_pvalue\tpercentage_overlap\trelative_enrichment\toverlapping\tnon_overlapping\tpermuted_mean\tpermuted_sd");
        mainOutputWriter.newLine();

        // Determine emperical pvalues for the enrichment
        for (String database : referenceSet.keySet()) {

            int[] curTrueOverlaps = trueOverlaps.get(database);
            double trueRatio = (double) curTrueOverlaps[0] / (curTrueOverlaps[0] + curTrueOverlaps[1]);

            int[][] curPermutedOverlaps = permutedOverlaps.get(database);
            double[] permutedRatios = new double[curPermutedOverlaps.length];
            for (int i = 0; i < curPermutedOverlaps.length; i++) {
                permutedRatios[i] = (double) curPermutedOverlaps[i][0] / (curPermutedOverlaps[i][0] + curPermutedOverlaps[i][1]);
            }

            // Determine rank of true value (one-sided)
            Arrays.sort(permutedRatios);
            int finalRank = 0;
            for (int i = 0; i < permutedRatios.length; i++) {
                if (trueRatio > permutedRatios[i]) {
                    finalRank++;
                }
            }

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(permutedRatios);
            double relativeEnrichment = trueRatio / descriptiveStatistics.getMean();
            if (relativeEnrichment < 1) {
                finalRank = permutedRatios.length - finalRank;
            }

            // Determine the emperical pvalue
            double empericalPvalue = 1 - ((finalRank + 0.5) / (permutedRatios.length + 1));

            // Determine the pseudo emperical pvalue

            double pseudoEmpericalPvalue = 1;
            if (descriptiveStatistics.getStandardDeviation() > 0) {
                NormalDistribution empericalNormalDist = new NormalDistribution(descriptiveStatistics.getMean(), descriptiveStatistics.getStandardDeviation());

                if (relativeEnrichment > 1) {
                    pseudoEmpericalPvalue = 1 - empericalNormalDist.cumulativeProbability(trueRatio);
                } else {
                    pseudoEmpericalPvalue = empericalNormalDist.cumulativeProbability(trueRatio);
                }
            }

            // If overlap is zero report pval of 1 to avoid confusion
            if (trueRatio == 0) {
                empericalPvalue = 1;
            }

            // Write the summary statistics
            mainOutputWriter.write(database);
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Double.toString(empericalPvalue));
            mainOutputWriter.write("\t");
            mainOutputWriter.write(Double.toString(pseudoEmpericalPvalue));
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
            LOGGER.info("Pvalue for " + database + ": " + empericalPvalue);

        }

        mainOutputWriter.flush();
        mainOutputWriter.close();

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
        LOGGER.info("done");

    }


    /**
     * Simulated a set of random genomic regions matching the query in number, size and chromosomal distribution
     *
     * @param query
     * @param regionsToSampleFrom
     * @return
     */
    private List<BedRecord> generatePermutedGenomicRegionsMatchingQuery(Collection<BedRecord> query, Map<String, ArrayList<BedRecord>> regionsToSampleFrom) {
        List<BedRecord> output = new ArrayList<>(query.size());

        for (BedRecord currentQuery : query) {
            int size = currentQuery.getEnd() - currentQuery.getStart();

            // Determine the available regions to sample from and select one randomly on the same chromosome as query
            ArrayList<BedRecord> possibleRegions = regionsToSampleFrom.get(currentQuery.getContig());
            int indexToSample = ThreadLocalRandom.current().nextInt(0, possibleRegions.size());
            BedRecord regionToSampleIn = possibleRegions.get(indexToSample);

            int newRegionMidPoint = ThreadLocalRandom.current().nextInt(regionToSampleIn.getStart(), regionToSampleIn.getEnd());
            output.add(new BedRecord(currentQuery.getContig(), newRegionMidPoint - size / 2, newRegionMidPoint + size / 2));
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

        LOGGER.info("Filtered " + removed + " records");
        return output;
    }
}
