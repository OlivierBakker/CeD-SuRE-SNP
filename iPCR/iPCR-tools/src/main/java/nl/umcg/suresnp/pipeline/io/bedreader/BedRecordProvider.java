package nl.umcg.suresnp.pipeline.io.bedreader;

import htsjdk.samtools.util.Locatable;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface BedRecordProvider {
    BedRecord getNextRecord() throws IOException;
    List<BedRecord> getBedRecordAsList() throws IOException;
    void close() throws IOException;
}
