package nl.umcg.mafsamplesubset;

public class AlleleFrequency implements Comparable<AlleleFrequency> {

    private final int index;
    private double afAllele1;
    private double afAllele2;
    private String allele1 = "";
    private String allele2 = "";
    private String variantId;
    private int missSnp = 0;

    public AlleleFrequency(int index, double afAllele1, double afAllele2, String allele1, String allele2) {
        this.index = index;
        this.afAllele1 = afAllele1;
        this.afAllele2 = afAllele2;
        this.allele1 = allele1;
        this.allele2 = allele2;
    }

    public AlleleFrequency(int index, double afAllele1, double afAllele2) {
        this.index = index;
        this.afAllele1 = afAllele1;
        this.afAllele2 = afAllele2;
    }

    public AlleleFrequency(int index, double afAllele1, double afAllele2, String allele1, String allele2, String variantId, int missSnp) {
        this.index = index;
        this.afAllele1 = afAllele1;
        this.afAllele2 = afAllele2;
        this.allele1 = allele1;
        this.allele2 = allele2;
        this.variantId = variantId;
        this.missSnp = missSnp;
    }

    @Override
    public int compareTo(AlleleFrequency other) {
        return Double.valueOf(this.afAllele2).compareTo(other.afAllele2);
    }

    public double getMaf() {
        if (this.afAllele1 < this.afAllele2) {
            return (this.afAllele1);
        } else {
            return (this.afAllele2);
        }
    }

    public int getIndex() {
        return index;
    }

    public double getAfAllele1() {
        return afAllele1;
    }

    public double getAfAllele2() {
        return afAllele2;
    }

    public String getAllele1() {
        return allele1;
    }

    public String getAllele2() {
        return allele2;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public int getMissSnp() {
        return missSnp;
    }

    public void setMissSnp(int missSnp) {
        this.missSnp = missSnp;
    }
}

