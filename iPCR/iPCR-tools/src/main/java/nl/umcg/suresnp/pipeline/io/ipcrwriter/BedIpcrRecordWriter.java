package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BedIpcrRecordWriter implements IpcrOutputWriter {

    protected BufferedWriter writer;
    private String[] barcodeCountFilesSampleNames;
    private final AdaptableScoreProvider scoreProvider;
    private final String sep = "\t";

    public BedIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames, AdaptableScoreProvider scoreProvider) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        this.writer = new GenericFile(outputPrefix.getPath() + FileExtensions.BED + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();

        if (barcodeCountFilesSampleNames != null) {
            this.barcodeCountFilesSampleNames = GenericFile.trimAllExtensionsFromFilenameArray(barcodeCountFilesSampleNames);
        }

        this.scoreProvider = scoreProvider;
    }

    public BedIpcrRecordWriter(File outputPrefix, boolean isZipped, AdaptableScoreProvider scoreProvider) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        this.writer = new GenericFile(outputPrefix.getPath() + FileExtensions.BED + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
        this.scoreProvider = scoreProvider;
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {

        double count = 0;
        if (scoreProvider != null) {
            count = scoreProvider.getScore(record);
        } else {
            count = 1;
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
