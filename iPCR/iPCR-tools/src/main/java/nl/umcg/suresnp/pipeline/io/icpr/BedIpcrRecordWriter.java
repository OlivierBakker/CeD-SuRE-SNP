package nl.umcg.suresnp.pipeline.io.icpr;

import htsjdk.samtools.SAMRecord;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class BedIpcrRecordWriter implements IpcrOutputWriter {

    protected OutputStream outputStream;
    protected BufferedWriter writer;
    private String[] barcodeCountFilesSampleNames;
    private final String sep = "\t";

    public BedIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix + ".bed"));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix + ".bed.gz"));
        }
        this.barcodeCountFilesSampleNames = new String[barcodeCountFilesSampleNames.length];

        // Clean filenames, trim all .
        int i = 0;
        for (String curFile: barcodeCountFilesSampleNames) {
            String tmp = new GenericFile(curFile).getBaseName();
            int idx = tmp.indexOf('.');
            this.barcodeCountFilesSampleNames[i] = tmp.substring(0, idx);
            i++;
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public BedIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix + ".bed"));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix + ".bed.gz"));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {

        // Flip arround so that the primary sam record is the first position
        SAMRecord tmp;
        if (record.getPrimarySamRecord().getReadNegativeStrandFlag()) {
            tmp = record.getPrimarySamRecord();
            record.setPrimarySamRecord(record.getPrimarySamRecordMate());
            record.setPrimarySamRecordMate(tmp);
        }

        writer.write(record.getPrimarySamRecord().getContig());
        writer.write(sep);

        writer.write(Integer.toString(record.getPrimarySamRecord().getAlignmentStart()));
        writer.write(sep);

        writer.write(Integer.toString(record.getPrimarySamRecordMate().getAlignmentEnd()));
        writer.write(sep);

        writer.write(record.getBarcode());
        writer.write(sep);

        writer.write(record.getPrimarySamRecord().getReadName());
        writer.write(sep);

        if (record.getBarcodeCountPerSample() != null) {
            for (String key: record.getBarcodeCountPerSample().keySet()) {
                writer.write(Integer.toString(record.getBarcodeCountPerSample().get(key)));
                writer.write(sep);
            }
        }

        writer.newLine();
    }

    @Override
    public void writeHeader() throws IOException {
        this.writeHeader("");
    }

    @Override
    public void writeHeader(String reason) throws IOException {
        writer.write("chr");
        writer.write(sep);
        writer.write("start");
        writer.write(sep);
        writer.write("end");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("readName");
        writer.write(sep);

        if (barcodeCountFilesSampleNames != null) {
            for (String key: barcodeCountFilesSampleNames) {
                writer.write(key);
                writer.write(sep);
            }

        }

        writer.newLine();

    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }

}
