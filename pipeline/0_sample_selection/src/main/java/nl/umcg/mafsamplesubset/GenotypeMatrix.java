package nl.umcg.mafsamplesubset;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.lang3.ArrayUtils;
import org.molgenis.genotype.GenotypeData;
import org.molgenis.genotype.variant.GeneticVariant;

import java.util.*;
import java.util.stream.Collectors;

public class GenotypeMatrix {

    private int[][] genotypeMatrix;
    private String[] variantNames;
    private String[] variantAlleles1;
    private String[] variantAlleles2;
    private String[] sampleNames;

    private Sample[] samples;
    private AlleleFrequency[] alleleFrequencies;

    public GenotypeMatrix(int[][] genotypeMatrix, String[] variantNames, String[] sampleNames, Sample[] samples) {
        this.genotypeMatrix = genotypeMatrix;
        this.variantNames = variantNames;
        this.sampleNames = sampleNames;
        this.samples = samples;
    }

    public GenotypeMatrix(int[][] genotypeMatrix, String[] variantNames, String[] sampleNames, Sample[] samples, String[] variantAlleles1, String[] variantAlleles2) {
        this.genotypeMatrix = genotypeMatrix;
        this.variantNames = variantNames;
        this.variantAlleles1 = variantAlleles1;
        this.variantAlleles2 = variantAlleles2;
        this.sampleNames = sampleNames;
        this.samples = samples;
    }

    public GenotypeMatrix(GenotypeData genotypeData, int nSnps){
        this.makeSampleGenotypes(genotypeData, nSnps);
    }

    private void makeSampleGenotypes(GenotypeData genotypeData, int nSnps) {

        int i = 0;
        //int j = 0;

        for (GeneticVariant variant : genotypeData) {

            if (i == 0) {
                this.sampleNames = genotypeData.getSampleNames();
                this.variantNames = new String[nSnps];
                this.variantAlleles1 = new String[nSnps];
                this.variantAlleles2 = new String[nSnps];
                this.genotypeMatrix = new int[this.variantNames.length][this.sampleNames.length];
                this.samples = new Sample[this.sampleNames.length];

                for (int j=0; j < this.sampleNames.length; j++) {
                    this.samples[j] = new Sample(j, this.sampleNames[j], 0);
                }

            }

            this.variantNames[i] = variant.getPrimaryVariantId();
            this.variantAlleles1[i] = variant.getVariantAlleles().getAllelesAsString().get(1);
            this.variantAlleles2[i] = variant.getVariantAlleles().getAllelesAsString().get(0);
            this.genotypeMatrix[i] = Utils.convertFloatsToIntArray(variant.getSampleDosages());
            //maf[i] = variant.getMinorAlleleFrequency();rsId
            //for(GenotypeRecord record : variant.getSampleGenotypeRecords()) {
            //    genotypeMatrix[i][j] = record.getSampleAlleles().toString();
            //    j++;
            //}
            //j = 0;
            i ++;
        }

    }

    // Based on sample objects
    public GenotypeMatrix subsetSamples(List<Sample> subset) {
        String[] samples = new String[subset.size()];
        int i = 0;
        for (Sample sample : subset) {
            samples[i] = sample.getSampleName();
            i++;
        }

        return (this.subsetSamples(samples));
    }


    // based on sample names
    public GenotypeMatrix subsetSamples(String[] subset) {

        int[][] newGenotypeMatrix = new int[this.variantNames.length][subset.length];
        Sample[] newSamples = new Sample[subset.length];

        for (int i=0; i<this.variantNames.length; i++) {

            int j = 0;
            for (String sample : subset) {
                int sampleIdx = ArrayUtils.indexOf(this.sampleNames, sample);
                newSamples[j] = this.samples[sampleIdx];
                newGenotypeMatrix[i][j] = this.genotypeMatrix[i][sampleIdx];
                j++;
            }
        }

        return(new GenotypeMatrix(newGenotypeMatrix, this.variantNames, subset, newSamples, this.variantAlleles1, this.variantAlleles2));
    }

    // based on indices
    public GenotypeMatrix subsetSamples(int[] subset) {

        int[][] newGenotypeMatrix = new int[this.variantNames.length][subset.length];
        String[] newSampleNames = new String[subset.length];
        Sample[] newSamples = new Sample[subset.length];

        for (int i=0; i<this.variantNames.length; i++) {

            int j = 0;
            for (int sampleIdx : subset) {
                newGenotypeMatrix[i][j] = this.genotypeMatrix[i][sampleIdx];
                newSampleNames[j] = this.sampleNames[sampleIdx];
                newSamples[j] = this.samples[sampleIdx];

                j++;
            }
        }

        return(new GenotypeMatrix(newGenotypeMatrix, this.variantNames, newSampleNames, newSamples));
    }

    public void calculateAlleleFrequencies() {

        this.alleleFrequencies = new AlleleFrequency[variantNames.length];

        for (int i = 0; i < variantNames.length; i++) {
            this.alleleFrequencies[i] = this.calculateAlleleFrequency(i);
        }
    }

    private AlleleFrequency calculateAlleleFrequency(int snpIndex) {
        Multiset<Integer> set = HashMultiset.create();

        // convert to list
        List<Integer> genotypes = Arrays.stream(genotypeMatrix[snpIndex])
                .boxed()
                .collect(Collectors.toList());
        set.addAll(genotypes);

        int missing = set.count(-1);
        double countZero = set.count(0);
        double countOne = set.count(1);
        double countTwo = set.count(2);
        double total = ((countZero * 2) + countOne) + ((countTwo * 2) + countOne);

        double afAllele1 = ((countZero * 2) + countOne) / total;
        double afAllele2 = ((countTwo * 2) + countOne) / total;

        return new AlleleFrequency(snpIndex, afAllele1, afAllele2,
                this.variantAlleles1[snpIndex], this.variantAlleles2[snpIndex], this.variantNames[snpIndex], missing);
    }

    public AlleleFrequency getMinAf() {

        AlleleFrequency smallestAllele = this.alleleFrequencies[0];
        double minValue = smallestAllele.getMaf();

        for (int i = 1; i < this.alleleFrequencies.length; i++) {
            if (this.alleleFrequencies[i].getMaf() < minValue) {
                minValue = this.alleleFrequencies[i].getMaf();
                smallestAllele = this.alleleFrequencies[i];
            }
        }
        return smallestAllele;
    }

    public AlleleFrequency getMinAlleleCount() {

        AlleleFrequency smallestAllele = this.alleleFrequencies[0];
        double minValue = smallestAllele.getMaf() * (this.samples.length - smallestAllele.getMissSnp());

        for (int i = 1; i < this.alleleFrequencies.length; i++) {
            double curValue = this.alleleFrequencies[i].getMaf() * (this.samples.length - this.alleleFrequencies[i].getMissSnp());

            if (curValue < minValue) {
                minValue = curValue;
                smallestAllele = this.alleleFrequencies[i];
            }
        }
        return smallestAllele;
    }


    // Calculate the total amount of minor alleles an individual carries
    // Homozygots are counted as 2, hetrozygous is 1
    public void calculateMinorAlleleCounts(Set<Integer> priorityVariants, boolean hetroOnly) {

        for (Integer variantIdx : priorityVariants) {
            AlleleFrequency alleleFrequency = this.alleleFrequencies[variantIdx];

            // Determine the dosage of the minor allele
            int minorDosage;

            if (alleleFrequency.getAfAllele1() < 0.5) {
                minorDosage = 0;
            } else {
                minorDosage = 2;
            }

            if (!hetroOnly) {
                // Get all the indices of the homozygous samples for a minor allele
                Set<Integer> homozygous = Utils.findIndicesEqual(this.genotypeMatrix[variantIdx], minorDosage);

                // For all the samples which are homo or hetrozygous add 1 to the count
                for (Integer idx : homozygous) {
                    this.samples[idx].setMinorAlleleCount(this.samples[idx].getMinorAlleleCount() + 2);
                }
            }

            // Get all the indices of the hetrozygous samples
            Set<Integer> hetrozygous = Utils.findIndicesEqual(this.genotypeMatrix[variantIdx], 1);

            // Fot all the samples which are homo or hetrozygous add 1 to the count
            for (Integer idx : hetrozygous) {
                this.samples[idx].setMinorAlleleCount(this.samples[idx].getMinorAlleleCount() + 1);
            }
        }
    }

    public double[] getMinorAlleleFrequencies() {
        double[] minorAlleleFrequncies = new double[this.alleleFrequencies.length];

        int i = 0;
        for (AlleleFrequency alleleFrequency : this.alleleFrequencies) {
            double af = alleleFrequency.getAfAllele2();
            if (af > 0.5) {
                af = 1 - af;
            }
            minorAlleleFrequncies[i] = af;
            i++;
        }

        return(minorAlleleFrequncies);
    }

    public int[][] getGenotypeMatrix() {
        return genotypeMatrix;
    }

    public String[] getVariantNames() {
        return variantNames;
    }

    public String[] getSampleNames() {
        return sampleNames;
    }

    public AlleleFrequency[] getAlleleFrequencies() {
        return alleleFrequencies;
    }

    public Sample[] getSamples() {
        return samples;
    }

    public void setSamples(Sample[] samples) {
        this.samples = samples;
    }
}
