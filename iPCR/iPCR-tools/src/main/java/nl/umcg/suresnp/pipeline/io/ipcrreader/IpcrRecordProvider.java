package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.filters.IpcrRecordFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface IpcrRecordProvider {
    IpcrRecord getNextRecord() throws IOException;
    List<IpcrRecord> getRecordsAsList() throws IOException;
    List<IpcrRecord> getRecordsAsList(List<IpcrRecordFilter> filters) throws IOException;
    Set<String> getBarcodeSet() throws IOException;
    String[] getCdnaSamples();
    void close() throws IOException;
}
