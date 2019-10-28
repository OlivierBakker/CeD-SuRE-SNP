package nl.umcg.suresnp.pipeline.io.icpr;


import nl.umcg.suresnp.pipeline.ipcrrecords.AlleleSpecificIpcrRecord;

import java.io.IOException;

public interface AlleleSpecificIpcrOutputWriter {

    void writeRecord(AlleleSpecificIpcrRecord record) throws IOException;
    void writeRecord(AlleleSpecificIpcrRecord record, String reason) throws IOException;
    void writeHeader() throws IOException;
    void writeHeader(String reason) throws IOException;
    void flushAndClose() throws IOException;
}
