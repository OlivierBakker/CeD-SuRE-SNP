package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class BedpeIpcrRecordWriter implements IpcrOutputWriter {

    protected BufferedWriter writer;
    private String[] barcodeCountFilesSampleNames;
    private final AdaptableScoreProvider scoreProvider;
    private final String sep = "\t";

    public BedpeIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames, AdaptableScoreProvider scoreProvider) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        this.writer = new GenericFile(outputPrefix.getPath() + FileExtensions.BEDPE + suffix).getAsBufferedWriter();

        if (barcodeCountFilesSampleNames != null) {
            this.barcodeCountFilesSampleNames = GenericFile.trimAllExtensionsFromFilenameArray(barcodeCountFilesSampleNames);
        }

        this.scoreProvider = scoreProvider;
    }

    public BedpeIpcrRecordWriter(File outputPrefix, boolean isZipped, AdaptableScoreProvider scoreProvider) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        this.writer = new GenericFile(outputPrefix.getPath() + FileExtensions.BEDPE + suffix).getAsBufferedWriter();
        this.scoreProvider = scoreProvider;
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

        writer.write(record.getQueryReadName());
        writer.write(sep);

        if (scoreProvider == null) {
            if (record.getBarcodeCountPerSample() != null) {
                for (String key : barcodeCountFilesSampleNames) {
                    writer.write(Integer.toString(record.getBarcodeCountPerSample().get(key)));
                    writer.write(sep);
                }
            }
        } else {
            writer.write(Double.toString(scoreProvider.getScore(record)));
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

        if (scoreProvider == null) {
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
        } else {
            writer.write(sep);
            writer.write(scoreProvider.getSamplesAsString());
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
