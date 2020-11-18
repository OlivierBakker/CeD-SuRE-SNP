package nl.umcg.suresnp.pipeline.io.bedreader;

import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface BedRecordProvider {
    BedRecord getNextRecord() throws IOException;
    List<BedRecord> getBedRecordAsList() throws IOException;
    void close() throws IOException;
}
