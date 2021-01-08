package nl.umcg.suresnp.pipeline.utils;

import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

import java.util.List;

public interface GenomicRegionPileup {

    void addIpcrRecord(IpcrRecord record);
    List<BedRecord> getPileup();
    BedRecord getNextRecord();
    boolean hasNext();
}
