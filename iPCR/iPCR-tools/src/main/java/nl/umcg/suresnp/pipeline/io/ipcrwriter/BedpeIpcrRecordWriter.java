package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class BedpeIpcrRecordWriter implements IpcrOutputWriter {

    protected BufferedWriter writer;
    private String[] barcodeCountFilesSampleNames;
    private final String sep = "\t";

    public BedpeIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames) throws IOException {

        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath()  + ".bedpe" + suffix).getAsBufferedWriter();
        this.barcodeCountFilesSampleNames = new String[barcodeCountFilesSampleNames.length];

        // Clean filenames, trim all .
        int i = 0;
        for (String curFile : barcodeCountFilesSampleNames) {
            String tmp = new GenericFile(curFile).getBaseName();
            int idx = tmp.indexOf('.');
            this.barcodeCountFilesSampleNames[i] = tmp.substring(0, idx);
            i++;
        }

    }

    public BedpeIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath()  + ".bedpe" + suffix).getAsBufferedWriter();
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {

        writer.write(record.getContig());
        writer.write(sep);

        writer.write(Integer.toString(record.getOrientationIndependentStart()));
        writer.write(sep);

        writer.write(Integer.toString(record.getOrientationIndependentEnd()));
        writer.write(sep);

        writer.write(record.getBarcode());
        writer.write(sep);

        writer.write(record.getPrimaryReadName());
        writer.write(sep);

        if (record.getBarcodeCountPerSample() != null) {
            for (String key : barcodeCountFilesSampleNames) {
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
            for (String key : barcodeCountFilesSampleNames) {
                int idx = key.indexOf('.');

                if (idx < 1) {
                    writer.write(key);
                } else {
                    writer.write(key.substring(0, idx));
                }
                writer.write(sep);
            }
        }

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
}
