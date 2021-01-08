package nl.umcg.suresnp.pipeline.records.bedrecord;

import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;

public class NarrowPeakRecord extends BedRecord implements Feature, Locatable {

    private String name;
    private char strand;
    private double signalValue;
    private double pValue;
    private double qValue;
    private int peak;

    public NarrowPeakRecord(String contig, int start, int stop, String name, double score, char strand, double signalValue, double pValue, double qValue, int peak) {
        super(contig, start, stop, score);
        this.name = name;
        this.strand = strand;
        this.signalValue = signalValue;
        this.pValue = pValue;
        this.qValue = qValue;
        this.peak = peak;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getStrandAsChar() {
        return strand;
    }

    public void setStrand(char strand) {
        this.strand = strand;
    }

    public double getSignalValue() {
        return signalValue;
    }

    public void setSignalValue(double signalValue) {
        this.signalValue = signalValue;
    }

    public double getpValue() {
        return pValue;
    }

    public void setpValue(double pValue) {
        this.pValue = pValue;
    }

    public double getqValue() {
        return qValue;
    }

    public void setqValue(double qValue) {
        this.qValue = qValue;
    }

    public int getPeak() {
        return peak;
    }

    public void setPeak(int peak) {
        this.peak = peak;
    }
}
