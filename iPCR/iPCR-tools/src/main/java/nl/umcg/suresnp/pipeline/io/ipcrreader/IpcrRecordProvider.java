package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.IOException;

public interface IpcrRecordProvider {
    IpcrRecord getNextRecord() throws IOException;
    void close() throws IOException;
}
