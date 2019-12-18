package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.filters.IpcrRecordFilter;

import java.io.IOException;
import java.util.List;

public interface IpcrRecordProvider {
    IpcrRecord getNextRecord() throws IOException;
    List<IpcrRecord> getRecordsAsList() throws IOException;
    List<IpcrRecord> getRecordsAsList(List<IpcrRecordFilter> filters) throws IOException;
    String[] getSamples();
    void close() throws IOException;
}
