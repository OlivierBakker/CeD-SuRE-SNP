package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class BedIpcrRecordWriter implements IpcrOutputWriter {

    protected BufferedWriter writer;
    private String sampleToWrite;
    private String[] barcodeCountFilesSampleNames;

    private final String sep = "\t";

    public BedIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames) throws IOException {
        String suffix = "";
        if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix + ".bed" + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();

        this.barcodeCountFilesSampleNames = new String[barcodeCountFilesSampleNames.length];
        // Clean filenames, trim all.
        int i = 0;
        for (String curFile : barcodeCountFilesSampleNames) {
            String tmp = new GenericFile(curFile).getBaseName();
            int idx = tmp.indexOf('.');
            this.barcodeCountFilesSampleNames[i] = tmp.substring(0, idx);
            i++;
        }
    }

    public BedIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        String suffix = "";
        if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix + ".bed" + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
    }


    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {

        int count = 0;
        if (sampleToWrite != null) {
            if (sampleToWrite.equals("IPCR")) {
                count = record.getIpcrDuplicateCount();
            } else {
                if (record.getBarcodeCountPerSample() != null) {
                    count = record.getBarcodeCountPerSample().get(sampleToWrite);
                }
            }
        }

        int i = 0;
        if (count > 0) {
            while (i < count) {
                writeBedRecord(record);
                i++;
            }
        }

    }

    private void writeBedRecord(IpcrRecord record) throws IOException {
        // chrom
        writer.write(record.getContig());
        writer.write(sep);

        // Make the positions 0 based and half open
        // chromStart
        writer.write(Integer.toString(record.getOrientationIndependentStart() - 1));
        writer.write(sep);

        // chromEnd
        writer.write(Integer.toString(record.getOrientationIndependentEnd()));
        writer.write(sep);

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
        writer.newLine();

    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
    }

    @Override
    public String[] getBarcodeCountFilesSampleNames() {
        return barcodeCountFilesSampleNames;
    }

    @Override
    public void setBarcodeCountFilesSampleNames(String[] barcodeCountFilesSampleNames) {
        this.barcodeCountFilesSampleNames = barcodeCountFilesSampleNames;
    }

    public void setSampleToWrite(String sample) {
        this.sampleToWrite = sample;
    }

}
