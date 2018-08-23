package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.IpcrRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class IpcrOutputFileWriter implements IpcrOutputWriter {

    protected OutputStream outputStream;
    protected BufferedWriter writer;

    public IpcrOutputFileWriter(File outputPrefix, boolean isZipped) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    @Override
    public void writeIPCRRecord(IpcrRecord record) throws IOException {
        writer.write(record.getSimpleOutputString(" "));
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }


}
