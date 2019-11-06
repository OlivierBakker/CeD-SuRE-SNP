package nl.umcg.suresnp.pipeline.ipcrrecords;

import java.util.Map;

public interface IpcrRecord {

    String getBarcode();

    void setBarcode(String barcode);

    String getReadName();

    void setReadName(String name);

    String getContig();

    void setContig(String contig);

    // Paired end reads
    int getPrimaryStart();

    void setPrimaryStart(int start);

    int getPrimaryEnd();

    void setPrimaryEnd(int end);

    int getMateStart();

    void setMateStart(int start);

    int getMateEnd();

    void setMateEnd(int end);


    int getPrimarySamFlags();

    void setPrimarySamFlags(int flag);

    int getMateSamFlags();

    void setMateSamFlags(int flag);

    int getPrimaryMappingQuality();

    void setPrimaryMappingQuality(int quality);

    int getMateMappingQuality();

    void setMateMappingQuality(int quality);

    String getPrimaryCigar();

    void setPrimaryCigar(String cigar);

    String getMateCigar();

    void setMateCigar(String cigar);

    char getPrimaryStrand();

    void setPrimaryStrand(char strand);

    char getMateStrand();

    void setMateStrand(char strand);

    Map<String, Integer> getBarcodeCountPerSample();

    void setBarcodeCountPerSample(Map<String, Integer> barcodeCountPerSample);


    void flipPrimaryAndMate();

}
