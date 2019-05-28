package nl.umcg.suresnp.pipeline.io.icpr;


import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.IOException;

public interface IpcrOutputWriter {

    void writeRecord(IpcrRecord record) throws IOException;
    void writeRecord(IpcrRecord record, String reason) throws IOException;
    void flushAndClose() throws IOException;
}
