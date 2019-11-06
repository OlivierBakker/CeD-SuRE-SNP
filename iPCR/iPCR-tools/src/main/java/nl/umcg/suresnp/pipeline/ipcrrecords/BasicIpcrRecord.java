package nl.umcg.suresnp.pipeline.ipcrrecords;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

public class BasicIpcrRecord implements IpcrRecord {

    private String barcode;
    private String readName;
    private String contig;
    private int primaryStart;
    private int primaryEnd;
    private int mateStart;
    private int mateEnd;
    private int primarySamFlags;
    private int mateSamFlags;
    private int primaryMappingQuality;
    private int mateMappingQuality;
    private String primaryCigar;
    private String mateCigar;
    private char primaryStrand;
    private char mateStrand;
    private Map<String, Integer> barcodeCountPerSample;

    public BasicIpcrRecord() {
    }

    public BasicIpcrRecord(String barcode, String readName, String contig, int primaryStart, int primaryEnd, int mateStart, int mateEnd, int primarySamFlags, int mateSamFlags, int primaryMappingQuality, int mateMappingQuality, String primaryCigar, String mateCigar, char primaryStrand, char mateStrand, Map<String, Integer> barcodeCountPerSample) {
        this.barcode = barcode;
        this.readName = readName;
        this.contig = contig;
        this.primaryStart = primaryStart;
        this.primaryEnd = primaryEnd;
        this.mateStart = mateStart;
        this.mateEnd = mateEnd;
        this.primarySamFlags = primarySamFlags;
        this.mateSamFlags = mateSamFlags;
        this.primaryMappingQuality = primaryMappingQuality;
        this.mateMappingQuality = mateMappingQuality;
        this.primaryCigar = primaryCigar;
        this.mateCigar = mateCigar;
        this.primaryStrand = primaryStrand;
        this.mateStrand = mateStrand;
        this.barcodeCountPerSample = barcodeCountPerSample;
    }

    @Override
    public String getBarcode() {
        return barcode;
    }

    @Override
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @Override
    public String getReadName() {
        return readName;
    }

    @Override
    public void setReadName(String readName) {
        this.readName = readName;
    }

    @Override
    public String getContig() {
        return contig;
    }

    @Override
    public void setContig(String contig) {
        this.contig = contig;
    }

    @Override
    public int getPrimaryStart() {
        return primaryStart;
    }

    @Override
    public void setPrimaryStart(int primaryStart) {
        this.primaryStart = primaryStart;
    }

    @Override
    public int getPrimaryEnd() {
        return primaryEnd;
    }

    @Override
    public void setPrimaryEnd(int primaryEnd) {
        this.primaryEnd = primaryEnd;
    }

    @Override
    public int getMateStart() {
        return mateStart;
    }

    @Override
    public void setMateStart(int mateStart) {
        this.mateStart = mateStart;
    }

    @Override
    public int getMateEnd() {
        return mateEnd;
    }

    @Override
    public void setMateEnd(int mateEnd) {
        this.mateEnd = mateEnd;
    }

    @Override
    public int getPrimarySamFlags() {
        return primarySamFlags;
    }

    @Override
    public void setPrimarySamFlags(int primarySamFlags) {
        this.primarySamFlags = primarySamFlags;
    }

    @Override
    public int getMateSamFlags() {
        return mateSamFlags;
    }

    @Override
    public void setMateSamFlags(int mateSamFlags) {
        this.mateSamFlags = mateSamFlags;
    }

    @Override
    public int getPrimaryMappingQuality() {
        return primaryMappingQuality;
    }

    @Override
    public void setPrimaryMappingQuality(int primaryMappingQuality) {
        this.primaryMappingQuality = primaryMappingQuality;
    }

    @Override
    public int getMateMappingQuality() {
        return mateMappingQuality;
    }

    @Override
    public void setMateMappingQuality(int mateMappingQuality) {
        this.mateMappingQuality = mateMappingQuality;
    }

    @Override
    public String getPrimaryCigar() {
        return primaryCigar;
    }

    @Override
    public void setPrimaryCigar(String primaryCigar) {
        this.primaryCigar = primaryCigar;
    }

    @Override
    public String getMateCigar() {
        return mateCigar;
    }

    @Override
    public void setMateCigar(String mateCigar) {
        this.mateCigar = mateCigar;
    }

    @Override
    public char getPrimaryStrand() {
        return primaryStrand;
    }

    @Override
    public void setPrimaryStrand(char primaryStrand) {
        this.primaryStrand = primaryStrand;
    }

    @Override
    public char getMateStrand() {
        return mateStrand;
    }

    @Override
    public void setMateStrand(char mateStrand) {
        this.mateStrand = mateStrand;
    }

    @Override
    public Map<String, Integer> getBarcodeCountPerSample() {
        return barcodeCountPerSample;
    }

    @Override
    public void setBarcodeCountPerSample(Map<String, Integer> barcodeCountPerSample) {
        this.barcodeCountPerSample = barcodeCountPerSample;
    }

    @Override
    public void flipPrimaryAndMate() {
        throw new NotImplementedException();
    }
}
