package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.IpcrRecord;

import java.io.IOException;

public class IpcrStdoutWriter implements IpcrOutputWriter {

    public IpcrStdoutWriter() {
    }

    @Override
    public void writeIPCRRecord(IpcrRecord record) throws IOException {

        System.out.print(record.getOutputString(" "));
        System.out.print("\n");
    }

    @Override
    public void close() throws IOException {

    }
}
