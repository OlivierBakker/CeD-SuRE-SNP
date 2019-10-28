package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.ipcrrecords.AlleleSpecificIpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class GenericIpcrRecordWriter implements IpcrOutputWriter {

    protected OutputStream outputStream;
    protected BufferedWriter writer;
    private final String sep = "\t";

    public GenericIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix + ".gz"));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }


    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        // Alignment info
        writer.write(record.getOutputString(sep));
        writer.newLine();
    }

    @Override
    public void writeHeader() throws IOException {
        this.writeHeader("");
    }


    @Override
    public void writeHeader(String reason) throws IOException {
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);

        writer.newLine();
    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }


}
