package nl.umcg.suresnp.pipeline.records.ipcrrecord;

import htsjdk.samtools.SAMRecord;
import nl.umcg.suresnp.pipeline.records.samrecord.PairedSamRecord;

import java.util.Map;

public class SamBasedIpcrRecord implements Comparable, IpcrRecord {

    private String barcode;
    private SAMRecord primarySamRecord;
    private SAMRecord primarySamRecordMate;
    private Map<String, Integer> barcodeCountPerSample;
    private int ipcrDuplicateCount;

    public SamBasedIpcrRecord(String barcode, SAMRecord primarySamRecord) {
        this.barcode = barcode;
        this.primarySamRecord = primarySamRecord;
    }

    public SamBasedIpcrRecord(String barcode, SAMRecord primarySamRecord, SAMRecord primarySamRecordMate) {
        this.barcode = barcode;
        this.primarySamRecord = primarySamRecord;
        this.primarySamRecordMate = primarySamRecordMate;
    }

    public SamBasedIpcrRecord(String barcode, SAMRecord primarySamRecord, SAMRecord primarySamRecordMate, Map<String, Integer> barcodeCountPerSample) {
        this.barcode = barcode;
        this.primarySamRecord = primarySamRecord;
        this.primarySamRecordMate = primarySamRecordMate;
        this.barcodeCountPerSample = barcodeCountPerSample;
    }

    public String getBarcode() {
        return barcode;
    }

    @Override
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
/*

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
*/

    public SAMRecord getPrimarySamRecord() {
        return primarySamRecord;
    }

    public void setPrimarySamRecord(SAMRecord primarySamRecord) {
        this.primarySamRecord = primarySamRecord;
    }

    public SAMRecord getPrimarySamRecordMate() {
        return primarySamRecordMate;
    }

    public void setPrimarySamRecordMate(SAMRecord primarySamRecordMate) {
        this.primarySamRecordMate = primarySamRecordMate;
    }


    public Map<String, Integer> getBarcodeCountPerSample() {
        return barcodeCountPerSample;
    }

    public void setBarcodeCountPerSample(Map<String, Integer> barcodeCountPerSample) {
        this.barcodeCountPerSample = barcodeCountPerSample;
    }

    @Override
    public void addBarcodeCount(String sample, Integer count) {
        this.barcodeCountPerSample.put(sample, count);
    }

    @Override
    public int getIpcrDuplicateCount() {
        return ipcrDuplicateCount;
    }

    @Override
    public void setIpcrDuplicateCount(int ipcrDuplicateCount) {
        this.ipcrDuplicateCount = ipcrDuplicateCount;
    }

    @Override
    public String getContig() {
        return primarySamRecord.getContig();
    }

    @Override
    public int getStart() {
        return getOrientationIndependentStart();
    }

    @Override
    public int getEnd() {
        return getOrientationIndependentEnd();
    }

    @Override
    public void setContig(String contig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryReadName() {
        return primarySamRecord.getReadName();
    }

    @Override
    public void setQueryReadName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMateReadName() {
        if (primarySamRecordMate != null) {
            return primarySamRecordMate.getReadName();
        } else {
            return null;
        }
    }

    @Override
    public void setMateReadName(String name) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int getQueryStart() {
        return primarySamRecord.getAlignmentStart();
    }

    @Override
    public void setQueryStart(int start) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getQueryEnd() {
        return primarySamRecord.getAlignmentEnd();
    }

    @Override
    public void setQueryEnd(int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMateStart() {
        if (primarySamRecordMate != null) {
            return primarySamRecordMate.getAlignmentStart();
        } else {
            return 0;
        }
    }

    @Override
    public void setMateStart(int start) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMateEnd() {
        if (primarySamRecordMate != null) {
            return primarySamRecordMate.getAlignmentEnd();
        } else {
            return 0;
        }
    }

    @Override
    public void setMateEnd(int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public char getQueryStrand() {
        if (primarySamRecord.getReadNegativeStrandFlag()) {
            return '-';
        } else {
            return '+';
        }
    }

    @Override
    public void setQueryStrand(char strand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public char getMateStrand() {
        if (primarySamRecordMate != null) {
            if (primarySamRecordMate.getReadNegativeStrandFlag()) {
                return '-';
            } else {
                return '+';
            }
        } else {
            return 0;
        }
    }

    @Override
    public void setMateStrand(char strand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getQuerySamFlags() {
        return primarySamRecord.getFlags();
    }

    @Override
    public void setQuerySamFlags(int flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMateSamFlags() {
        if (primarySamRecordMate != null) {
            return primarySamRecordMate.getFlags();
        } else {
            return 0;
        }
    }

    @Override
    public void setMateSamFlags(int flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getQueryMappingQuality() {
        return primarySamRecord.getMappingQuality();
    }

    @Override
    public void getQueryMappingQuality(int quality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMateMappingQuality() {
        if (primarySamRecordMate != null) {
            return primarySamRecordMate.getMappingQuality();
        } else {
            return 0;
        }
    }

    @Override
    public void setMateMappingQuality(int quality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQuerySigar() {
        return primarySamRecord.getCigarString();
    }

    @Override
    public void setQuerySigar(String cigar) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMateCigar() {
        if (primarySamRecordMate != null) {
            return primarySamRecordMate.getCigarString();
        } else {
            return null;
        }
    }

    @Override
    public void setMateCigar(String cigar) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flipPrimaryAndMate() {
        SAMRecord tmp = primarySamRecord;
        primarySamRecord = primarySamRecordMate;
        primarySamRecordMate = tmp;
    }

    @Override
    public char getOrientation() {
        if (getQueryStrand() == '+' && getMateStrand() == '-'){
            return '+';
        } else if (getQueryStrand() == '-' && getMateStrand() == '+') {
            return '-';
        } else {
            return '.';
        }
    }


    @Override
    public int compareTo(Object o) {
        SamBasedIpcrRecord other = (SamBasedIpcrRecord) o;
        return (barcode.compareTo(other.getBarcode()));
    }

    @Override
    public boolean isFullyInsideWindow(int start, int stop) {
        return isStartInWindow(start, stop) && isStopInWindow(start, stop);
    }

    @Override
    public boolean isStartInWindow(int start, int stop) {
        return getOrientationIndependentStart() >= start && getOrientationIndependentStart() <= stop;
    }

    @Override
    public boolean isStopInWindow(int start, int stop) {
        return getOrientationIndependentEnd() >= start && getOrientationIndependentEnd() <= stop;
    }

    @Override
    public int getOrientationIndependentStart() {
        if (getQueryStrand() == '+') {
            return getQueryStart();
        } else if (getQueryStrand() == '-') {
            return getMateStart();
        } else {
            throw new IllegalStateException("Strand must be either + or -");
        }
    }

    @Override
    public int getOrientationIndependentEnd() {
        if (getQueryStrand() == '+') {
            return getMateEnd();
        } else if (getQueryStrand() == '-') {
            return getQueryEnd();
        } else {
            throw new IllegalStateException("Strand must be either + or -");
        }
    }

    @Override
    public int getMappingQualitySum() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMappedBaseCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updatePositions(PairedSamRecord samRecord) {

    }
}
