package nl.umcg.mafsamplesubset;

public class PermutedSubset {

    private String[] samples;

    // Store the minimum maf with the corresponding permutations
    private AlleleFrequency minimumMaf;

    // Store the mean maf for the corresponding permutations
    private double meanMaf;

    // Store the count of mafs greater then the target maf
    private int targetCount;

    // Array of allele frequencies of the subset
    private AlleleFrequency[] alleleFrequencies;


    public PermutedSubset() {
    }

    public PermutedSubset(String[] samples, AlleleFrequency minimumMaf, double meanMaf, int targetCount, AlleleFrequency[] alleleFrequencies) {
        this.samples = samples;
        this.minimumMaf = minimumMaf;
        this.meanMaf = meanMaf;
        this.targetCount = targetCount;
        this.alleleFrequencies = alleleFrequencies;
    }

    public String[] getSamples() {
        return samples;
    }

    public void setSamples(String[] samples) {
        this.samples = samples;
    }

    public int getMinimumAlleleCount() {
        return (int) Math.round(this.minimumMaf.getMaf() * (this.samples.length - this.minimumMaf.getMissSnp()));
    }

    public double getMinimumMaf() {
        return minimumMaf.getMaf();
    }

    public double getMeanMaf() {
        return meanMaf;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public AlleleFrequency[] getAlleleFrequencies() {
        return alleleFrequencies;
    }

}
