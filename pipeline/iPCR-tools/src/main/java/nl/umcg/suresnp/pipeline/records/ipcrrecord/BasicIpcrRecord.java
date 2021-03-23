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
    private String queryReadName;
    private String mateReadName;
    private String contig;
    private int queryStart;
    private int queryEnd;
    private int mateStart;
    private int mateEnd;
    private int querySamFlags;
    private int mateSamFlags;
    private int queryMappingQuality;
    private int mateMappingQuality;
    private String querySigar;
    private String mateCigar;
    private char queryStrand;
    private char mateStrand;
    private Map<String, Integer> barcodeCountPerSample;
    private int ipcrDuplicateCount;

    public BasicIpcrRecord() {
    }

    public BasicIpcrRecord(String contig, int queryStart, int mateEnd) {
        this.contig = contig;
        this.queryStart = queryStart;
        this.mateEnd = mateEnd;
    }

    public BasicIpcrRecord(String barcode, String queryReadName, String contig, int queryStart, int queryEnd, int mateStart, int mateEnd, int querySamFlags, int mateSamFlags, int queryMappingQuality, int mateMappingQuality, String querySigar, String mateCigar, char queryStrand, char mateStrand, Map<String, Integer> barcodeCountPerSample) {
        this.barcode = barcode;
        this.queryReadName = queryReadName;
        this.contig = contig;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
        this.mateStart = mateStart;
        this.mateEnd = mateEnd;
        this.querySamFlags = querySamFlags;
        this.mateSamFlags = mateSamFlags;
        this.queryMappingQuality = queryMappingQuality;
        this.mateMappingQuality = mateMappingQuality;
        this.querySigar = querySigar;
        this.mateCigar = mateCigar;
        this.queryStrand = queryStrand;
        this.mateStrand = mateStrand;
        this.barcodeCountPerSample = barcodeCountPerSample;
    }

    public BasicIpcrRecord(IpcrRecord record) {
        this.barcode = record.getBarcode();
        this.queryReadName = record.getQueryReadName();
        this.contig = record.getContig();
        this.queryStart = record.getQueryStart();
        this.queryEnd = record.getQueryEnd();
        this.mateStart = record.getMateStart();
        this.mateEnd = record.getMateEnd();
        this.querySamFlags = record.getQuerySamFlags();
        this.mateSamFlags = record.getMateSamFlags();
        this.queryMappingQuality = record.getQueryMappingQuality();
        this.mateMappingQuality = record.getMateMappingQuality();
        this.querySigar = record.getQuerySigar();
        this.mateCigar = record.getMateCigar();
        this.queryStrand = record.getQueryStrand();
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
    public String getQueryReadName() {
        return queryReadName;
    }

    @Override
    public void setQueryReadName(String readName) {
        this.queryReadName = readName;
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
    public int getQueryStart() {
        return queryStart;
    }

    @Override
    public void setQueryStart(int primaryStart) {
        this.queryStart = primaryStart;
    }

    @Override
    public int getQueryEnd() {
        return queryEnd;
    }

    @Override
    public void setQueryEnd(int primaryEnd) {
        this.queryEnd = primaryEnd;
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
    public int getQuerySamFlags() {
        return querySamFlags;
    }

    @Override
    public void setQuerySamFlags(int primarySamFlags) {
        this.querySamFlags = primarySamFlags;
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
    public int getQueryMappingQuality() {
        return queryMappingQuality;
    }

    @Override
    public void getQueryMappingQuality(int primaryMappingQuality) {
        this.queryMappingQuality = primaryMappingQuality;
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
    public String getQuerySigar() {
        return querySigar;
    }

    @Override
    public void setQuerySigar(String primaryCigar) {
        this.querySigar = primaryCigar;
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
    public char getQueryStrand() {
        return queryStrand;
    }

    @Override
    public void setQueryStrand(char primaryStrand) {
        this.queryStrand = primaryStrand;
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


    @Deprecated
    @Override
    public boolean isFullyInsideWindow(int start, int stop) {
        return isStartInWindow(start, stop) && isStopInWindow(start, stop);
    }

    @Deprecated
    @Override
    public boolean isStartInWindow(int start, int stop) {
        return getOrientationIndependentStart() >= start && getOrientationIndependentStart() <= stop;
    }

    @Deprecated
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
            return getQueryStart();
        }

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
        //if (getPrimaryStrand() == 0) {
        //    primaryStrand = '+';
        // }

        if (isNotPaired) {
            return getQueryEnd();
        }

        // Minus is correct here
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
        int totalMq = getQueryMappingQuality() + getMateMappingQuality();
        return totalMq;
    }


    @Override
    public int getMappedBaseCount() {
        int r1length = getQueryEnd() - getQueryStart();
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
        if ((getQueryStrand() == '-') == rec1.getReadNegativeStrandFlag()) {
            // Santiy checks
            int dist = Math.abs(queryStart - rec1.getStart());
            if (dist > 1000) {
                LOGGER.warn("New position more then 1kb away, are you sure this is correct?");
            }

            queryStart = rec1.getStart();
            queryEnd = rec1.getEnd();
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
                queryStart = rec2.getStart();
                queryEnd = rec2.getEnd();
            } else {
                isNotPaired = true;
                queryStart = rec1.getStart();
                queryEnd = rec1.getEnd();
            }
            mateStart  = rec1.getStart();
            mateEnd = rec1.getEnd();
        }
    }
}
