package nl.umcg.suresnp.pipeline.io.icpr;


import nl.umcg.suresnp.pipeline.ipcrrecords.AlleleSpecificBamBasedIpcrRecord;

import java.io.IOException;

public interface AlleleSpecificIpcrOutputWriter {

    void writeRecord(AlleleSpecificBamBasedIpcrRecord record) throws IOException;
    void writeRecord(AlleleSpecificBamBasedIpcrRecord record, String reason) throws IOException;
    void writeHeader() throws IOException;
    void writeHeader(String reason) throws IOException;
    void flushAndClose() throws IOException;
}
