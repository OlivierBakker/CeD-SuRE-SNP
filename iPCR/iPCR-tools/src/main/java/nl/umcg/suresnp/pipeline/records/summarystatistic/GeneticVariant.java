package nl.umcg.suresnp.pipeline.records.summarystatistic;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;

import java.io.Serializable;

/**
 * The type Snp.
 */
public class GeneticVariant extends Interval implements Feature, Locatable {

    /**
     * The primary variant id.
     */
    protected String primaryVariantId;

    /**
     * The Allele 1.
     */
    protected String allele1;

    /**
     * The Allele 2.
     */
    protected String allele2;

    public GeneticVariant(GeneticVariant other) {
        super(other.getContig(), other.getStart(), other.getEnd());
        this.primaryVariantId = other.getPrimaryVariantId();
        this.allele1 = other.getAllele1();
        this.allele2 = other.getAllele2();
    }

    public GeneticVariant(String primaryVariantId, String allele1, String allele2, int position, String sequenceName) {
        super(sequenceName, position, position);
        this.primaryVariantId = primaryVariantId;
        this.allele1 = allele1;
        this.allele2 = allele2;
    }

    /**
     * Gets rs id.
     *
     * @return the rs id
     */
    public String getPrimaryVariantId() {
        return primaryVariantId;
    }

    /**
     * Sets rs id.
     *
     * @param primaryVariantId the rs id
     */
    public void setPrimaryVariantId(String primaryVariantId) {
        this.primaryVariantId = primaryVariantId;
    }

    /**
     * Gets allele 1.
     *
     * @return the allele 1
     */
    public String getAllele1() {
        return allele1;
    }

    /**
     * Sets allele 1.
     *
     * @param allele1 the allele 1
     */
    public void setAllele1(String allele1) {
        this.allele1 = allele1;
    }

    /**
     * Gets allele 2.
     *
     * @return the allele 2
     */
    public String getAllele2() {
        return allele2;
    }

    /**
     * Sets allele 2.
     *
     * @param allele2 the allele 2
     */
    public void setAllele2(String allele2) {
        this.allele2 = allele2;
    }

    /**
     * Gets position.
     *
     * @return the position
     */
    public int getPosition() {
        return this.getStart();
    }


    /**
     * Gets human chromosome.
     *
     * @return the human chromosome
     */
    public int getHumanChromosome() {

        if (getContig().toLowerCase().equals("x")) {
            return (23);
        } else if (getContig().toLowerCase().equals("y")) {
            return (24);
        } else if (getContig().toLowerCase().equals("mt")) {
            return (25);
        } else {
            String curSeq = getContig();
            return Integer.parseInt(curSeq.replace("chr", ""));
        }
    }

    /**
     * Is transition boolean.
     *
     * @return the boolean
     */
    public boolean isTransition() {

        // Check if a variant is a transition
        if (allele1.equals("C") && allele2.equals("T") || allele2.equals("T") && allele1.equals("C")){
            return true;
        }

        if (allele1.equals("A") && allele2.equals("G") || allele2.equals("A") && allele1.equals("G")) {
            return true;
        }

        return false;

    }
}
