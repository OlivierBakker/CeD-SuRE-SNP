package nl.umcg.suresnp.pipeline.records.bedrecord;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;
import nl.umcg.suresnp.pipeline.utils.B37GenomeInfo;

public class BedRecord extends Interval implements Feature, Locatable {

    private double score;

    public BedRecord(NarrowPeakRecord record) {
        super(record.getContig(), record.getStart(), record.getEnd());
        this.score = record.getSignalValue();
    }

    public BedRecord(String contig, int start, int end) {
        super(contig, start, end);
    }

    public BedRecord(String contig, int start, int end, double score) {
        super(contig, start, end);
        this.score = score;
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
        return this.getContig() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + score;
    }
}
