package nl.umcg.suresnp.pipeline.ipcrrecords;

import htsjdk.samtools.SAMRecord;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

public class SamBasedIpcrRecord implements Comparable, IpcrRecord {

    private String barcode;
    private SAMRecord primarySamRecord;
    private SAMRecord primarySamRecordMate;
    private Map<String, Integer> barcodeCountPerSample;

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

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

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
    public String getContig() {
        return primarySamRecord.getContig();
    }

    @Override
    public void setContig(String contig) {
        throw new NotImplementedException();
    }

    @Override
    public String getPrimaryReadName() {
        return primarySamRecord.getReadName();
    }

    @Override
    public void setPrimaryReadName(String name) {
        throw new NotImplementedException();
    }

    @Override
    public String getMateReadName() {
        return primarySamRecordMate.getReadName();
    }

    @Override
    public void setMateReadName(String name) {
        throw new NotImplementedException();
    }


    @Override
    public int getPrimaryStart() {
        return primarySamRecord.getAlignmentStart();
    }

    @Override
    public void setPrimaryStart(int start) {
        throw new NotImplementedException();
    }

    @Override
    public int getPrimaryEnd() {
        return primarySamRecord.getAlignmentEnd();
    }

    @Override
    public void setPrimaryEnd(int end) {
        throw new NotImplementedException();
    }

    @Override
    public int getMateStart() {
        return primarySamRecordMate.getAlignmentStart();
    }

    @Override
    public void setMateStart(int start) {
        throw new NotImplementedException();
    }

    @Override
    public int getMateEnd() {
        return primarySamRecordMate.getAlignmentEnd();
    }

    @Override
    public void setMateEnd(int end) {
        throw new NotImplementedException();
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
        throw new NotImplementedException();
    }

    @Override
    public char getMateStrand() {
        if (primarySamRecordMate.getReadNegativeStrandFlag()) {
            return '-';
        } else {
            return '+';
        }
    }

    @Override
    public void setMateStrand(char strand) {
        throw new NotImplementedException();
    }

    @Override
    public int getPrimarySamFlags() {
        return primarySamRecord.getFlags();
    }

    @Override
    public void setPrimarySamFlags(int flag) {
        throw new NotImplementedException();
    }

    @Override
    public int getMateSamFlags() {
        return primarySamRecordMate.getFlags();
    }

    @Override
    public void setMateSamFlags(int flag) {
        throw new NotImplementedException();
    }

    @Override
    public int getPrimaryMappingQuality() {
        return primarySamRecord.getMappingQuality();
    }

    @Override
    public void setPrimaryMappingQuality(int quality) {
        throw new NotImplementedException();
    }

    @Override
    public int getMateMappingQuality() {
        return primarySamRecordMate.getMappingQuality();
    }

    @Override
    public void setMateMappingQuality(int quality) {
        throw new NotImplementedException();
    }

    @Override
    public String getPrimaryCigar() {
        return primarySamRecord.getCigarString();
    }

    @Override
    public void setPrimaryCigar(String cigar) {
        throw new NotImplementedException();
    }

    @Override
    public String getMateCigar() {
        return primarySamRecordMate.getCigarString();
    }

    @Override
    public void setMateCigar(String cigar) {
        throw new NotImplementedException();
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
}
