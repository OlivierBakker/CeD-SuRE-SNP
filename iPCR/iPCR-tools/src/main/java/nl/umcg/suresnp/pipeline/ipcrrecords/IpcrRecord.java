package nl.umcg.suresnp.pipeline.ipcrrecords;

import htsjdk.samtools.SAMRecord;

import java.util.Map;

public class IpcrRecord implements Comparable {

    private String barcode;
    private SAMRecord primarySamRecord;
    private SAMRecord primarySamRecordMate;
    private Map<String, Integer> barcodeCountPerSample;

    public IpcrRecord(String barcode, SAMRecord primarySamRecord) {
        this.barcode = barcode;
        this.primarySamRecord = primarySamRecord;
    }

    public IpcrRecord(String barcode, SAMRecord primarySamRecord, SAMRecord primarySamRecordMate) {
        this.barcode = barcode;
        this.primarySamRecord = primarySamRecord;
        this.primarySamRecordMate = primarySamRecordMate;
    }

    public IpcrRecord(String barcode, SAMRecord primarySamRecord, SAMRecord primarySamRecordMate,Map<String, Integer> barcodeCountPerSample) {
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
    public int compareTo(Object o) {
        IpcrRecord other = (IpcrRecord) o;
        return (barcode.compareTo(other.getBarcode()));
    }
}
