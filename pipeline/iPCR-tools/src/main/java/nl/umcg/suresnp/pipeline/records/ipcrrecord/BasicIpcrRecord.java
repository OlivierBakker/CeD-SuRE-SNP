package nl.umcg.suresnp.pipeline.records.ipcrrecord;


import htsjdk.samtools.SAMRecord;
import nl.umcg.suresnp.pipeline.records.samrecord.PairedSamRecord;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Map;

public class BasicIpcrRecord implements IpcrRecord, Serializable {

    private static final Logger LOGGER = Logger.getLogger(BasicIpcrRecord.class);

    private boolean isNotPaired = false;
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

    public BasicIpcrRecord(IpcrRecord record) {
        this.barcode = record.getBarcode();
        this.primaryReadName = record.getPrimaryReadName();
        this.contig = record.getContig();
        this.primaryStart = record.getPrimaryStart();
        this.primaryEnd = record.getPrimaryEnd();
        this.mateStart = record.getMateStart();
        this.mateEnd = record.getMateEnd();
        this.primarySamFlags = record.getPrimarySamFlags();
        this.mateSamFlags = record.getMateSamFlags();
        this.primaryMappingQuality = record.getPrimaryMappingQuality();
        this.mateMappingQuality = record.getMateMappingQuality();
        this.primaryCigar = record.getPrimaryCigar();
        this.mateCigar = record.getMateCigar();
        this.primaryStrand = record.getPrimaryStrand();
        this.barcodeCountPerSample = record.getBarcodeCountPerSample();
        this.ipcrDuplicateCount = record.getIpcrDuplicateCount();
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
    public int getStart() {
        return getOrientationIndependentStart();
    }

    @Override
    public int getEnd() {
        return getOrientationIndependentEnd();
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
    public void addBarcodeCount(String sample, Integer count) {
        barcodeCountPerSample.put(sample, count);
    }

    @Override
    public int getIpcrDuplicateCount() {
        return ipcrDuplicateCount;
    }

    @Override
    public void setIpcrDuplicateCount(int ipcrDuplicateCount) {
        this.ipcrDuplicateCount = ipcrDuplicateCount;
    }

    @Deprecated
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
        if (isNotPaired) {
            return getPrimaryStart();
        }

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

        if (isNotPaired) {
            return getPrimaryEnd();
        }

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

    @Override
    public void updatePositions(PairedSamRecord samRecord) {

        SAMRecord rec1 = samRecord.getOne();
        SAMRecord rec2 = samRecord.getTwo();

        // Santiy checks
        if (!contig.equals(rec1.getContig())){
            throw new IllegalArgumentException("Trying to update position, but contigs mismatch, this is likely not what you want");
        }

        // Quick check which record is which, works only because iPCR record must be proper pair
        if ((getPrimaryStrand() == '-') == rec1.getReadNegativeStrandFlag()) {
            // Santiy checks
            int dist = Math.abs(primaryStart - rec1.getStart());
            if (dist > 1000) {
                LOGGER.warn("New position more then 1kb away, are you sure this is correct?");
            }

            primaryStart = rec1.getStart();
            primaryEnd = rec1.getEnd();
            if (rec2 !=null) {
                mateStart  = rec2.getStart();
                mateEnd = rec2.getEnd();
            } else {
                isNotPaired = true;
                mateStart  = rec1.getStart();
                mateEnd = rec1.getEnd();
            }

        } else {
            if (rec2 !=null) {
                primaryStart  = rec2.getStart();
                primaryEnd = rec2.getEnd();
            } else {
                isNotPaired = true;
                primaryStart  = rec1.getStart();
                primaryEnd = rec1.getEnd();
            }
            mateStart  = rec1.getStart();
            mateEnd = rec1.getEnd();
        }
    }
}
