package nl.umcg.suresnp.pipeline.io.bedreader;

import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface BedRecordProvider {
    List<BedRecord> getBedRecordAsList() throws IOException;
    void close() throws IOException;
}
