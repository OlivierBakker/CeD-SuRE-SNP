package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.IpcrRecord;

import java.io.*;

public class SimpleIpcrOutputFileWriter extends IpcrOutputFileWriter  {


    public SimpleIpcrOutputFileWriter(File outputPrefix, boolean isZipped) throws IOException {
        super(outputPrefix, isZipped);
    }

    @Override
    public void writeIPCRRecord(IpcrRecord record) throws IOException {
        writer.write(record.getSimpleOutputString(" "));
        writer.newLine();
    }

}
