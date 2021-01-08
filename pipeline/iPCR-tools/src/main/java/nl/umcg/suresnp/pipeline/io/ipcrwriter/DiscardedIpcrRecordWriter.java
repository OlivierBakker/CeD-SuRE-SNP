package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DiscardedIpcrRecordWriter implements IpcrOutputWriter {

    protected OutputStream outputStream;
    protected BufferedWriter writer;
    private final String sep = "\t";

    public DiscardedIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath() + FileExtensions.IPCR_DISCARD + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        // Alignment info

        writer.write(reason);
        writer.write(sep);
        writer.write(record.getBarcode());
        writer.write(sep);
        writer.write(record.getPrimaryReadName());
        writer.write(sep);

        if (record.getMateReadName() != null) {
            writer.write(record.getMateReadName());
            writer.write(sep);
        } else {
            writer.write("NA");
            writer.write(sep);
        }

        writer.write(record.getContig());
        writer.write(sep);

        writer.write(Integer.toString(record.getPrimaryStart()));
        writer.write(sep);
        writer.write(Integer.toString(record.getPrimaryEnd()));
        writer.write(sep);

        if (record.getMateStart() != 0) {
            writer.write(Integer.toString(record.getMateStart()));
            writer.write(sep);
            writer.write(Integer.toString(record.getMateEnd()));
            writer.write(sep);
        } else {
            writer.write("NA");
            writer.write(sep);
            writer.write("NA");
            writer.write(sep);
        }

        writer.write(Integer.toString(record.getPrimarySamFlags()));
        writer.write(sep);

        if (record.getMateSamFlags() != 0) {
            writer.write(Integer.toString(record.getMateSamFlags()));
        } else {
            writer.write("NA");
        }

        writer.write(sep);
        writer.write(Integer.toString(record.getPrimaryMappingQuality()));
        writer.write(sep);

        if (record.getMateMappingQuality() != 0) {
            writer.write(Integer.toString(record.getMateMappingQuality()));
        } else {
            writer.write("NA");
        }

        writer.write(sep);
        writer.write(record.getPrimaryCigar());
        writer.write(sep);

        if (record.getMateCigar() != null) {
            writer.write(record.getMateCigar());
        } else {
            writer.write("NA");
        }
        writer.write(sep);

        writer.write(record.getPrimaryStrand());

        if (record.getMateStrand() != 0){
            writer.write(sep);
            writer.write(record.getMateStrand());
        } else {
            writer.write("NA");
        }
        writer.newLine();
    }

    @Override
    public void writeHeader() throws IOException {
        this.writeHeader("");
    }

    @Override
    public void writeHeader(String reason) throws IOException {

        if (reason.length() >= 1) {
            writer.write("reason");
            writer.write(sep);
        }
        writer.write("barcode");
        writer.write(sep);
        writer.write("readName");
        writer.write(sep);
        writer.write("readNameMate");
        writer.write(sep);
        writer.write("chromosome");
        writer.write(sep);
        writer.write("readOneStart");
        writer.write(sep);
        writer.write("readOneEnd");
        writer.write(sep);
        writer.write("readTwoStart");
        writer.write(sep);
        writer.write("readTwoEnd");
        writer.write(sep);
        writer.write("readOneFlag");
        writer.write(sep);
        writer.write("readTwoFlag");
        writer.write(sep);
        writer.write("readOneMQ");
        writer.write(sep);
        writer.write("readTwoMQ");
        writer.write(sep);
        writer.write("readOneCigar");
        writer.write(sep);
        writer.write("readTwoCigar");
        writer.write(sep);
        writer.write("readOneStrand");
        writer.write(sep);
        writer.write("readTwoStrand");
        writer.newLine();
    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }

    @Override
    public String[] getBarcodeCountFilesSampleNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBarcodeCountFilesSampleNames(String[] barcodeCountFilesSampleNames) {
        throw new UnsupportedOperationException();
    }


}
