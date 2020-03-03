package nl.umcg.suresnp.pipeline.io.ipcrwriter;


import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificIpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.SamBasedAlleleSpecificIpcrRecord;

import java.io.IOException;

public interface AlleleSpecificIpcrOutputWriter {

    void writeRecord(AlleleSpecificIpcrRecord record) throws IOException;
    void writeRecord(AlleleSpecificIpcrRecord record, String reason) throws IOException;
    void writeHeader() throws IOException;
    void writeHeader(String reason) throws IOException;
    void flushAndClose() throws IOException;
    String[] getBarcodeCountFilesSampleNames();
    void setBarcodeCountFilesSampleNames(String[] barcodeCountFilesSampleNames);

}
