package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.IpcrRecord;

import java.io.IOException;

public interface IpcrOutputWriter {

    void writeIPCRRecord(IpcrRecord record) throws IOException;
    void close() throws IOException;
}
