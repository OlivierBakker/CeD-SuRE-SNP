package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface IpcrRecordProvider extends Iterable<IpcrRecord> {
    IpcrRecord getNextRecord() throws IOException;
    List<IpcrRecord> getRecordsAsList() throws IOException;
    List<IpcrRecord> getRecordsAsList(List<IpcrRecordFilter> filters) throws IOException;
    Set<String> getBarcodeSet() throws IOException;
    String[] getCdnaSamples();
    void close() throws IOException;
}
