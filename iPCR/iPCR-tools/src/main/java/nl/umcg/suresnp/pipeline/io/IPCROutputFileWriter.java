package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.SAMRecord;
import nl.umcg.suresnp.pipeline.IPCRRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class IPCROutputFileWriter implements IPCROutputWriter {

    private OutputStream outputStream;
    private BufferedWriter writer;

    public IPCROutputFileWriter(File outputPrefix, boolean isZipped) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public void writeIPCRRecord(IPCRRecord record) throws IOException {
        writer.write(record.getOutputString("\t"));
        writer.newLine();
    }

    public void close() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }


}
