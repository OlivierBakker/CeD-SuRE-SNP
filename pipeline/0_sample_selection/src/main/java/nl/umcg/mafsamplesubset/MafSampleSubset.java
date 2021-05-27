package nl.umcg.mafsamplesubset;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.DoubleStream;

import static java.lang.System.exit;

public class MafSampleSubset {

    private static Logger LOGGER = Logger.getLogger(MafSampleSubset.class);

    public static void main(String[] args){

        // Init commandline parameters
        CommandLine cmd = null;
        final MafSampleSubsetParameters parameters = new MafSampleSubsetParameters();

        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(parameters.getOptions(), args);
        } catch (ParseException e) {
            LOGGER.error(e);
            parameters.printHelp();
            exit(1);
        }

        try {

            // ------------------------------
            // Setting options
            // ------------------------------

            // Read genotype data
            GenotypeMatrix genotypeMatrix = FileReader.readGenotypeData(cmd);
            genotypeMatrix.calculateAlleleFrequencies();

            // Output prefix
            String outputPrefix = cmd.getOptionValue('o').trim();

            // The minimun maf per snp to shoot for in the subset
            double targetMaf = Double.parseDouble(cmd.getOptionValue('u').trim());

            // The size of the subset
            int subsetSize = Integer.parseInt(cmd.getOptionValue('s').trim());

            // The threshold to consider a low maf variant
            double lowMafThresh = Double.parseDouble(cmd.getOptionValue('l').trim());

            // The amount of minor alleles a sample has to have to be prioritized
            int minMinorAllele = Integer.parseInt(cmd.getOptionValue('h').trim());

            // The maximum amount of random permutations to test
            int maxPerm = Integer.parseInt(cmd.getOptionValue('p').trim());

            // Use only heterozygous individuals
            boolean hetroOnly = cmd.hasOption('v');

            // ------------------------------
            // Sample prioritization
            // ------------------------------

            // Determine the indices of low maf variants to prioritize samples on
            Set<Integer> priorityVariants = Utils.findIndicesLess(genotypeMatrix.getMinorAlleleFrequencies(), lowMafThresh);

            // Calculate the number of minor alleles each individual carries
            genotypeMatrix.calculateMinorAlleleCounts(priorityVariants, hetroOnly);

            // Generate a list of the prioritized samples
            Sample[] samples = genotypeMatrix.getSamples();
            int[] sampleIds = new int[samples.length];
            List<Sample> prioritizedSamples = new ArrayList<>();

            int i = 0;
            for (Sample sample : samples) {
                sampleIds[i] = sample.getIndex();
                if (sample.getMinorAlleleCount() > minMinorAllele) {
                    prioritizedSamples.add(sample);
                }
                i++;
            }

            // Sort the counted list from low to high
            Collections.sort(prioritizedSamples);
            // Flip the order so the highest are on top
            Collections.reverse(prioritizedSamples);

            // Remove the non prioritized samples from the genotype matrix
            genotypeMatrix = genotypeMatrix.subsetSamples(prioritizedSamples);

            LOGGER.info(prioritizedSamples.size() + " samples with at least " + minMinorAllele + " minor allele");

            // Calculate the MAFs for the top 30 ranked ones
            GenotypeMatrix topSamples = genotypeMatrix.subsetSamples(prioritizedSamples.subList(0, subsetSize));
            topSamples.calculateAlleleFrequencies();

            PermutedSubset top = evaluateSubset(topSamples, targetMaf);

            FileWriter.writePermutedSubset(new GenericFile(outputPrefix + "_top.log"), top);
            FileWriter.writeAlleleFrequencies(new GenericFile(outputPrefix + "_top.af"), top);
            FileWriter.writeSampleList(new GenericFile(outputPrefix + "_top.samples"), top);

            // Init the storage variable
            // TODO: remove storage of all permutations. Only keep the best one
            PermutedSubset[] permutations = new PermutedSubset[maxPerm + 1];

            // Keep track of index of the best permutation
            int bestPerm = 0;
            permutations[0] = top;
            int loggingInterval = 10000;

            GenericFile file = new GenericFile(outputPrefix + ".allMinAf");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath()), "UTF-8"));

            // Evaluate a maximum of maxPerm random subsets of n=subsetSize
            for (int j = 1; j < maxPerm + 1; j++) {

                // Select a subset of random samples of n=subsetSize
                String[] subset = new String[subsetSize];
                int p = 0;
                while (p < subsetSize){
                    Random random = new Random();
                    int n = random.nextInt(genotypeMatrix.getSampleNames().length);

                    if ( !ArrayUtils.contains(subset, genotypeMatrix.getSampleNames()[n])) {
                        subset[p] = genotypeMatrix.getSampleNames()[n];
                        p ++;
                    } else {
                        LOGGER.warn("Sample is already in random subset");
                    }
                }

                 // Extract them from the genotype matrix
                GenotypeMatrix newGenotypeMatrix = genotypeMatrix.subsetSamples(subset);
                newGenotypeMatrix.calculateAlleleFrequencies();

                permutations[j] = evaluateSubset(newGenotypeMatrix, targetMaf);
                bw.write(permutations[j].getMinimumAlleleCount() + "\t" + permutations[j].getMinimumMaf() + "\t" + permutations[j].getMeanMaf());
                bw.newLine();

                // Determine the best permutation
                if (permutations[bestPerm].getMinimumAlleleCount() < permutations[j].getMinimumAlleleCount() &&
                        permutations[bestPerm].getMeanMaf() < permutations[j].getMeanMaf() + 0.01) {
                    bestPerm = j;

                    LOGGER.debug("New optimal permutation found: " + bestPerm);
                }

                if (j % loggingInterval == 0) {
                    LOGGER.info(j + " subsets evaluated");
                }
            }

            bw.flush();
            bw.close();

            // --------------------------------------
            // Logging final output
            // --------------------------------------
            LOGGER.info(permutations[bestPerm].getMinimumMaf() + " is the smallest MAF");
            LOGGER.info(permutations[bestPerm].getMeanMaf() + " is the mean MAF");
            LOGGER.info(permutations[bestPerm].getTargetCount() + " SNPs larger then the target MAF");

            // Writing final output
            FileWriter.writePermutedSubset(new GenericFile(outputPrefix + ".log"), permutations[bestPerm]);
            FileWriter.writeAlleleFrequencies(new GenericFile(outputPrefix + ".af"), permutations[bestPerm]);
            FileWriter.writeSampleList(new GenericFile(outputPrefix + ".samples"), permutations[bestPerm]);

        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    private static PermutedSubset evaluateSubset(GenotypeMatrix newGenotypeMatrix, double targetMaf) {

        // Calculate the mean maf
        double meanMaf = DoubleStream.of(newGenotypeMatrix.getMinorAlleleFrequencies()).sum() / newGenotypeMatrix.getVariantNames().length;

        // Calculate how many SNPs are above the target maf
        int count = 0;
        for (AlleleFrequency af : newGenotypeMatrix.getAlleleFrequencies()) {
            if (af.getMaf() > targetMaf) {
                count +=1;
            }
        }

        // Save the results
        return (new PermutedSubset(newGenotypeMatrix.getSampleNames(),
                newGenotypeMatrix.getMinAlleleCount(),
                meanMaf,
                count,
                newGenotypeMatrix.getAlleleFrequencies()));

    }

}
