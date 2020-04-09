package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class BedpeIpcrRecordWriter implements IpcrOutputWriter {

    protected BufferedWriter writer;
    private String[] barcodeCountFilesSampleNames;
    private final String sep = "\t";

    public BedpeIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath() + FileExtensions.BEDPE + suffix).getAsBufferedWriter();

        if (barcodeCountFilesSampleNames != null) {
            this.barcodeCountFilesSampleNames = GenericFile.trimAllExtensionsFromFilenameArray(barcodeCountFilesSampleNames);
        }
    }

    public BedpeIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath() + FileExtensions.BEDPE + suffix).getAsBufferedWriter();
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
