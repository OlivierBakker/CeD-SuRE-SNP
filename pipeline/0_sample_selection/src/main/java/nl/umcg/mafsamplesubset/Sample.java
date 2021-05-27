package nl.umcg.mafsamplesubset;

public class Sample implements Comparable<Sample> {

    private int index;
    private String sampleName;
    private int minorAlleleCount;

    public Sample(int index, String sampleName, int minorAlleleCount) {
        this.index = index;
        this.sampleName = sampleName;
        this.minorAlleleCount = minorAlleleCount;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public int getMinorAlleleCount() {
        return minorAlleleCount;
    }

    public void setMinorAlleleCount(int minorAlleleCount) {
        this.minorAlleleCount = minorAlleleCount;
    }

    @Override
    public int compareTo(Sample other) {
        return Integer.compare(this.minorAlleleCount, other.getMinorAlleleCount());
    }
}
