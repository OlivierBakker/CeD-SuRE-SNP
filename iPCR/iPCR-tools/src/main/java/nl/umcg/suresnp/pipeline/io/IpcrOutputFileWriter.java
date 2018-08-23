package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.IpcrRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class IpcrOutputFileWriter implements IpcrOutputWriter {

    private OutputStream outputStream;
    private BufferedWriter writer;

    public IpcrOutputFileWriter(File outputPrefix, boolean isZipped) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public void writeIPCRRecord(IpcrRecord record) throws IOException {
        writer.write(record.getOutputString(" "));
        writer.newLine();
    }

    public void close() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }


}
