package nl.umcg.suresnp.pipeline.io.ipcrwriter;


import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificSamBasedIpcrRecord;

import java.io.IOException;

public interface AlleleSpecificIpcrOutputWriter {

    void writeRecord(AlleleSpecificSamBasedIpcrRecord record) throws IOException;
    void writeRecord(AlleleSpecificSamBasedIpcrRecord record, String reason) throws IOException;
    void writeHeader() throws IOException;
    void writeHeader(String reason) throws IOException;
    void flushAndClose() throws IOException;
}
