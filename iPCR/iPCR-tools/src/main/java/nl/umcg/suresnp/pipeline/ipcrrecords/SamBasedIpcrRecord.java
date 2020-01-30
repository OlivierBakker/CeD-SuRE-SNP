package nl.umcg.suresnp.pipeline.ipcrrecords;

import htsjdk.samtools.SAMRecord;


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
    public void setContig(String contig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrimaryReadName() {
        return primarySamRecord.getReadName();
    }

    @Override
    public void setPrimaryReadName(String name) {
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
    public int getPrimaryStart() {
        return primarySamRecord.getAlignmentStart();
    }

    @Override
    public void setPrimaryStart(int start) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPrimaryEnd() {
        return primarySamRecord.getAlignmentEnd();
    }

    @Override
    public void setPrimaryEnd(int end) {
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
    public char getPrimaryStrand() {
        if (primarySamRecord.getReadNegativeStrandFlag()) {
            return '-';
        } else {
            return '+';
        }
    }

    @Override
    public void setPrimaryStrand(char strand) {
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
    public int getPrimarySamFlags() {
        return primarySamRecord.getFlags();
    }

    @Override
    public void setPrimarySamFlags(int flag) {
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
    public int getPrimaryMappingQuality() {
        return primarySamRecord.getMappingQuality();
    }

    @Override
    public void setPrimaryMappingQuality(int quality) {
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
    public String getPrimaryCigar() {
        return primarySamRecord.getCigarString();
    }

    @Override
    public void setPrimaryCigar(String cigar) {
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
    public int compareTo(Object o) {
        SamBasedIpcrRecord other = (SamBasedIpcrRecord) o;
        return (barcode.compareTo(other.getBarcode()));
    }

    @Override
    public boolean isPartiallyOverlappingWindow(int start, int stop) {
        return isStartInWindow(start, stop) || isStopInWindow(start, stop);
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
        if (getPrimaryStrand() == '+') {
            return getPrimaryStart();
        } else if (getPrimaryStrand() == '-') {
            return getMateStart();
        } else {
            throw new IllegalStateException("Strand must be either + or -");
        }
    }

    @Override
    public int getOrientationIndependentEnd() {
        if (getPrimaryStrand() == '+') {
            return getMateEnd();
        } else if (getPrimaryStrand() == '-') {
            return getPrimaryEnd();
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
}
