package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.IOException;

public interface IpcrOutputWriter {

    void writeIPCRRecord(IpcrRecord record) throws IOException;
    void close() throws IOException;
}
