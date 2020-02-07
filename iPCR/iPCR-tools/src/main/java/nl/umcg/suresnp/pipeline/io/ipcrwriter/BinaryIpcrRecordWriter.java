package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.records.ipcrrecords.IpcrRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class BinaryIpcrRecordWriter implements IpcrOutputWriter {

    private ObjectOutputStream outputStream;
    // This is a bit of a fail, I was expecting the java serialization to be pretty decent, but a zipped text file
    // Is much quicker and more space efficient. If I have some spare time I might implement a custom binary format.
    public BinaryIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        if (!isZipped) {
            RandomAccessFile file = new RandomAccessFile(outputPrefix + ".dat", "rw");
            outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file.getFD())));
        } else {
            outputStream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(outputPrefix + ".dat.gz")));
        }
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        outputStream.writeObject(record);
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        outputStream.writeObject(record);
    }

    @Override
    public void writeHeader() throws IOException {
        throw new UnsupportedOperationException("Not implemented as not relevant here");
    }

    @Override
    public void writeHeader(String reason) throws IOException {
        throw new UnsupportedOperationException("Not implemented as not relevant here");
    }

    @Override
    public void flushAndClose() throws IOException {
        outputStream.flush();
        outputStream.close();
    }

    @Override
    public String[] getBarcodeCountFilesSampleNames() {
        throw new UnsupportedOperationException("Not implemented as not relevant here");
    }

    @Override
    public void setBarcodeCountFilesSampleNames(String[] barcodeCountFilesSampleNames) {
        throw new UnsupportedOperationException("Not implemented as not relevant here");
    }
}
