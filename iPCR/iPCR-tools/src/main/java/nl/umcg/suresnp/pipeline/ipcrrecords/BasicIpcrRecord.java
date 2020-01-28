package nl.umcg.suresnp.pipeline.ipcrrecords;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

public class BasicIpcrRecord implements IpcrRecord {

    private String barcode;
    private String primaryReadName;
    private String mateReadName;
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
    private int ipcrDuplicateCount;

    public BasicIpcrRecord() {
    }

    public BasicIpcrRecord(String contig, int primaryStart, int mateEnd) {
        this.contig = contig;
        this.primaryStart = primaryStart;
        this.mateEnd = mateEnd;
    }

    public BasicIpcrRecord(String barcode, String primaryReadName, String contig, int primaryStart, int primaryEnd, int mateStart, int mateEnd, int primarySamFlags, int mateSamFlags, int primaryMappingQuality, int mateMappingQuality, String primaryCigar, String mateCigar, char primaryStrand, char mateStrand, Map<String, Integer> barcodeCountPerSample) {
        this.barcode = barcode;
        this.primaryReadName = primaryReadName;
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
    public String getPrimaryReadName() {
        return primaryReadName;
    }

    @Override
    public void setPrimaryReadName(String readName) {
        this.primaryReadName = readName;
    }

    @Override
    public String getMateReadName() {
        return mateReadName;
    }

    @Override
    public void setMateReadName(String mateReadName) {
        this.mateReadName = mateReadName;
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
    public int getIpcrDuplicateCount() {
        return ipcrDuplicateCount;
    }

    @Override
    public void setIpcrDuplicateCount(int ipcrDuplicateCount) {
        this.ipcrDuplicateCount = ipcrDuplicateCount;
    }

    @Override
    public void flipPrimaryAndMate() {
        /*String tmpPrimaryReadName = primaryReadName;
        int tmpPrimaryStart = primaryStart;
        int tmpPrimaryEnd = primaryEnd;
        int tmpPrimarySamFlags = primarySamFlags;
        int tmpPrimaryMappingQual = primaryMappingQuality;
        String tmpPrimaryCigar = primaryCigar;
        char tmpPrimaryStrand = primaryStrand;

        primaryReadName = mateReadName;
        primaryStart = mateStart;
        primaryEnd = mateEnd;
        primarySamFlags = mateSamFlags;
        primaryMappingQuality = mateMappingQuality;
        primaryCigar = mateCigar;
        primaryStrand = mateStrand;

        mateReadName = tmpPrimaryReadName;
        mateStart = tmpPrimaryStart;
        mateEnd = tmpPrimaryEnd;
        mateSamFlags = tmpPrimarySamFlags;
        mateMappingQuality = tmpPrimaryMappingQual;
        mateCigar = tmpPrimaryCigar;
        mateStrand = tmpPrimaryStrand;*/
        throw new UnsupportedOperationException("Temporarly disabled for sanity check");
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

       // if (getPrimaryStrand() == 0) {
        //    primaryStrand = '+';
        //}
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
        //if (getPrimaryStrand() == 0) {
        //    primaryStrand = '+';
        // }
        // Minus is correct here
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
        int totalMq = getPrimaryMappingQuality() + getMateMappingQuality();
        return totalMq;
    }


    @Override
    public int getMappedBaseCount() {
        int r1length = getPrimaryEnd() - getPrimaryStart();
        int r2length = getMateEnd() - getMateStart();
        return r1length + r2length;
    }
}
