package nl.umcg.suresnp.pipeline.records.samrecord;

import htsjdk.samtools.SAMRecord;

public class PairedSamRecord {
    private SAMRecord one;
    private SAMRecord two;
    private String source;

    public PairedSamRecord(SAMRecord one, SAMRecord two, String source) {
        this.one = one;
        this.two = two;
        this.source = source;
    }

    public PairedSamRecord(SAMRecord one, String source) {
        this.one = one;
        this.source = source;

    }

    public String getReadName() {
        return one.getReadName().split(" ")[0];
    }

    public SAMRecord getOne() {
        return one;
    }

    public void setOne(SAMRecord one) {
        this.one = one;
    }

    public SAMRecord getTwo() {
        return two;
    }

    public void setTwo(SAMRecord two) {
        this.two = two;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getReadPositionAtReferencePosition(int pos, boolean returnLastBaseIfDeleted) {
        if (two != null) {
            if (one.getAlignmentStart() > two.getAlignmentStart()) {
                return two.getReadPositionAtReferencePosition(pos, returnLastBaseIfDeleted);
            }
        }
        return one.getReadPositionAtReferencePosition(pos, returnLastBaseIfDeleted);
    }

    public String getReadString() {
        if (two != null) {
            if (one.getAlignmentStart() > two.getAlignmentStart()) {
                return two.getReadString();
            }
        }
        return one.getReadString();

    }
}
