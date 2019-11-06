package nl.umcg.suresnp.pipeline.io.ipcrwriter;


import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.IOException;

public interface IpcrOutputWriter {

    void writeRecord(IpcrRecord record) throws IOException;
    void writeRecord(IpcrRecord record, String reason) throws IOException;
    void writeHeader() throws IOException;
    void writeHeader(String reason) throws IOException;
    void flushAndClose() throws IOException;
}
