package nl.umcg.suresnp.pipeline.records.bedrecord;

import nl.umcg.suresnp.pipeline.utils.B37GenomeInfo;

public class BedRecord {

    private String contig;
    private int start;
    private int stop;

    public BedRecord(String contig, int start, int stop) {
        this.contig = contig;
        this.start = start;
        this.stop = stop;
    }

    public String getContig() {
        return contig;
    }

    public void setContig(String contig) {
        this.contig = contig;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public boolean overlaps(BedRecord other) {
        if (!contig.equals(other.getContig())) {
            return false;
        }
        return B37GenomeInfo.isInWindow(other.getStart(), start, stop) || B37GenomeInfo.isInWindow(other.getStop(), start, stop);
    }

    public boolean isFullyOverlapping(BedRecord other) {
        if (!contig.equals(other.getContig())) {
            return false;
        }
        return B37GenomeInfo.isInWindow(other.getStart(), start, stop) && B37GenomeInfo.isInWindow(other.getStop(), start, stop);
    }
}
