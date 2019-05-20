package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.IOException;

public class SimpleIpcrStdoutWriter implements IpcrOutputWriter {

    public SimpleIpcrStdoutWriter() {
    }

    @Override
    public void writeIPCRRecord(IpcrRecord record) throws IOException {

        System.out.print(record.getSimpleOutputString(" "));
        System.out.print("\n");
    }

    @Override
    public void close() throws IOException {

    }

}
