package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.SAMRecord;
import nl.umcg.suresnp.pipeline.IPCRRecord;

import java.io.IOException;

public interface IPCROutputWriter {

    void writeIPCRRecord(IPCRRecord record) throws IOException;
    void close() throws IOException;
}
