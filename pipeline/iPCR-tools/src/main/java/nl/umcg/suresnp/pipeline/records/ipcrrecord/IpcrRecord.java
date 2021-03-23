package nl.umcg.suresnp.pipeline.records.ipcrrecord;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;
import nl.umcg.suresnp.pipeline.records.samrecord.PairedSamRecord;

import java.util.Map;

public interface IpcrRecord extends Feature, Locatable {

    String getBarcode();

    void setBarcode(String barcode);

    String getQueryReadName();

    void setQueryReadName(String name);

    String getMateReadName();

    void setMateReadName(String name);

    String getContig();

    void setContig(String contig);

    // Paired end reads
    int getQueryStart();

    void setQueryStart(int start);

    int getQueryEnd();

    void setQueryEnd(int end);

    int getMateStart();

    void setMateStart(int start);

    int getMateEnd();

    void setMateEnd(int end);

    int getQuerySamFlags();

    void setQuerySamFlags(int flag);

    int getMateSamFlags();

    void setMateSamFlags(int flag);

    int getQueryMappingQuality();

    void getQueryMappingQuality(int quality);

    int getMateMappingQuality();

    void setMateMappingQuality(int quality);

    String getQuerySigar();

    void setQuerySigar(String cigar);

    String getMateCigar();

    void setMateCigar(String cigar);

    char getQueryStrand();

    void setQueryStrand(char strand);

    char getMateStrand();

    void setMateStrand(char strand);

    char getOrientation();

    Map<String, Integer> getBarcodeCountPerSample();

    void setBarcodeCountPerSample(Map<String, Integer> barcodeCountPerSample);

    void addBarcodeCount(String sample, Integer count);

    int getIpcrDuplicateCount();

    void setIpcrDuplicateCount(int ipcrDuplicateCount);

    void flipPrimaryAndMate();

    boolean isFullyInsideWindow(int start, int stop);

    boolean isStartInWindow(int start, int stop);

    boolean isStopInWindow(int start, int stop);

    int getOrientationIndependentStart();

    int getOrientationIndependentEnd();

    int getMappingQualitySum();

    int getMappedBaseCount();

    void updatePositions(PairedSamRecord samRecord);

}
