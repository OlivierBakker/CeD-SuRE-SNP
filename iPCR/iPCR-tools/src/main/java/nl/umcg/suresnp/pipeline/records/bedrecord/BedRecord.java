package nl.umcg.suresnp.pipeline.records.bedrecord;

import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;
import nl.umcg.suresnp.pipeline.utils.B37GenomeInfo;

public class BedRecord implements Feature, Locatable {

    private String contig;
    private int start;
    private int end;
    private double score;

    public BedRecord(NarrowPeakRecord record) {
        this.contig = record.getContig();
        this.start = record.getStart();
        this.end = record.getEnd();
        this.score = record.getSignalValue();
    }

    public BedRecord(String contig, int start, int end) {
        this.contig = contig;
        this.start = start;
        this.end = end;
    }

    public BedRecord(String contig, int start, int end, double score) {
        this.contig = contig;
        this.start = start;
        this.end = end;
        this.score = score;
    }

    @Override
    public String getContig() {
        return contig;
    }

    public void setContig(String contig) {
        this.contig = contig;
    }

    @Override
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    public void setEnd(int stop) {
        this.end = stop;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

/*    // TODO: replaced by Locatable default impl
    public boolean overlaps(BedRecord other) {
        if (!contig.equals(other.getContig())) {
            return false;
        }

        if (B37GenomeInfo.isInWindow(other.getStart(), start, end)) {
            return  true;
        }

        if (B37GenomeInfo.isInWindow(other.getEnd(), start, end)) {
            return true;
        }

        if (B37GenomeInfo.isInWindow(start, other.getStart(), other.getEnd())) {
            return  true;
        }

        if (B37GenomeInfo.isInWindow(end, other.getStart(), other.getEnd())) {
            return true;
        }

        return false;
    }

    // TODO: replaced by Locatable default impl
    public boolean isFullyOverlapping(BedRecord other) {
        if (!contig.equals(other.getContig())) {
            return false;
        }
        return B37GenomeInfo.isInWindow(other.getStart(), start, end) && B37GenomeInfo.isInWindow(other.getEnd(), start, end);
    }*/

    public String toBedString() {
        return contig + "\t" + start + "\t" + end + "\t" + score;
    }
}
