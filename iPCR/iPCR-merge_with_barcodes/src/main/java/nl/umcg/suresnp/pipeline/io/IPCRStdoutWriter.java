package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.SAMRecord;
import nl.umcg.suresnp.pipeline.IPCRRecord;
import nl.umcg.suresnp.pipeline.io.IPCROutputWriter;

import java.io.IOException;

public class IPCRStdoutWriter implements IPCROutputWriter {

    public IPCRStdoutWriter() {
    }

    @Override
    public void writeIPCRRecord(IPCRRecord record) throws IOException {

        System.out.print(record.getOutputString("\t"));
        System.out.print("\n");
    }

    @Override
    public void close() throws IOException {

    }
}
