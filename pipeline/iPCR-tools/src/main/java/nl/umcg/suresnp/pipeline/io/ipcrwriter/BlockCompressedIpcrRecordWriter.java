package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.util.LittleEndianOutputStream;
import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static htsjdk.tribble.index.tabix.TabixFormat.GENERIC_FLAGS;

public class BlockCompressedIpcrRecordWriter extends GenericIpcrRecordWriter implements IpcrOutputWriter {

    public static TabixFormat IPCR_FORMAT = new TabixFormat(GENERIC_FLAGS, 3, 4, 5, '#', 1);
    private BufferedWriter barcodeWriter;
    private BlockCompressedOutputStream outputStream;
    private TabixIndexCreator indexCreator;
    private LittleEndianOutputStream indexWriter;

    public BlockCompressedIpcrRecordWriter(String outputPrefix) throws IOException {
        super(null, "\t");
        constructWriters(outputPrefix);
        //TabixFormat.
    }

    public BlockCompressedIpcrRecordWriter(String outputPrefix, String[] barcodeCountFilesSampleNames) throws IOException {
        super(barcodeCountFilesSampleNames, "\t");
        constructWriters(outputPrefix);
    }

    private void constructWriters(String outputPrefix) throws IOException {
        barcodeWriter = new GenericFile(outputPrefix + FileExtensions.IPCR_BARCODES + ".gz").getAsBufferedWriter();
        outputStream = new BlockCompressedOutputStream(outputPrefix + FileExtensions.IPCR_INDEXED);
        indexCreator = new TabixIndexCreator(IPCR_FORMAT);
        indexWriter = new LittleEndianOutputStream(new BlockCompressedOutputStream(outputPrefix + FileExtensions.IPCR_INDEX));
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        barcodeWriter.write(record.getBarcode());
        barcodeWriter.newLine();

        indexCreator.addFeature(record, outputStream.getFilePointer());
        writeRecordToWriter(record);
    }

    @Override
    public void flushAndClose() throws IOException {
        Index index = indexCreator.finalizeIndex(outputStream.getFilePointer());
        index.write(indexWriter);
        indexWriter.close();

        outputStream.flush();
        outputStream.close();

        barcodeWriter.flush();
        barcodeWriter.close();
    }

    @Override
    protected void write(String s) throws IOException {
        outputStream.write(s.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    protected void writeNewLine() throws IOException {
        outputStream.write("\n".getBytes(StandardCharsets.US_ASCII));
    }


}
