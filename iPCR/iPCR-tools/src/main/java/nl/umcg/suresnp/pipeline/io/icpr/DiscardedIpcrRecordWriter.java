package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class DiscardedIpcrRecordWriter implements IpcrOutputWriter {

    protected OutputStream outputStream;
    protected BufferedWriter writer;
    private final String sep = "\t";

    public DiscardedIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {

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

        writer.write(reason);
        writer.write(sep);
        writer.write(record.getBarcode());
        writer.write(sep);
        writer.write(record.getPrimarySamRecord().getReadName());
        writer.write(sep);

        if (record.getPrimarySamRecordMate() != null) {
            writer.write(record.getPrimarySamRecordMate().getReadName());
            writer.write(sep);
        } else {
            writer.write("NA");
            writer.write(sep);
        }

        writer.write(record.getPrimarySamRecord().getContig());
        writer.write(sep);

        writer.write(Integer.toString(record.getPrimarySamRecord().getAlignmentStart()));
        writer.write(sep);
        writer.write(Integer.toString(record.getPrimarySamRecord().getAlignmentEnd()));
        writer.write(sep);

        if (record.getPrimarySamRecordMate() != null) {
            writer.write(Integer.toString(record.getPrimarySamRecordMate().getAlignmentStart()));
            writer.write(sep);
            writer.write(Integer.toString(record.getPrimarySamRecordMate().getAlignmentEnd()));
            writer.write(sep);
        } else {
            writer.write("NA");
            writer.write(sep);
            writer.write("NA");
            writer.write(sep);
        }

        writer.write(Integer.toString(record.getPrimarySamRecord().getMappingQuality()));
        writer.write(sep);

        if (record.getPrimarySamRecordMate() != null) {
            writer.write(Integer.toString(record.getPrimarySamRecordMate().getMappingQuality()));
        } else {
            writer.write("NA");
        }

        writer.write(sep);
        writer.write(record.getPrimarySamRecord().getCigarString());
        writer.write(sep);

        if (record.getPrimarySamRecordMate() != null) {
            writer.write(record.getPrimarySamRecordMate().getCigarString());
        } else {
            writer.write("NA");
        }
        writer.write(sep);

        if (record.getPrimarySamRecord().getReadNegativeStrandFlag()) {
            writer.write("-");
        } else {
            writer.write("+");
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
        writer.write("readOneMQ");
        writer.write(sep);
        writer.write("readTwoMQ");
        writer.write(sep);
        writer.write("readOneCigar");
        writer.write(sep);
        writer.write("readTwoCigar");
        writer.write(sep);
        writer.write("orientation");
        writer.newLine();
    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }

}
