package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GenericIpcrRecordWriter implements IpcrOutputWriter {

    private BufferedWriter barcodeWriter;
    private BufferedWriter coreWriter;
    private String[] barcodeCountFilesSampleNames;
    private String sep = "\t";
    private static final Logger LOGGER = Logger.getLogger(GenericIpcrRecordWriter.class);


    public GenericIpcrRecordWriter(String[] barcodeCountFilesSampleNames, String sep) {
        if (barcodeCountFilesSampleNames != null) {
            this.barcodeCountFilesSampleNames = GenericFile.trimAllExtensionsFromFilenameArray(barcodeCountFilesSampleNames);
        }
        this.sep = sep;
    }

    public GenericIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames) throws IOException {
        constructWriters(outputPrefix.getPath(), isZipped);

        if (barcodeCountFilesSampleNames != null) {
            this.barcodeCountFilesSampleNames = GenericFile.trimAllExtensionsFromFilenameArray(barcodeCountFilesSampleNames);
        }
    }

    public GenericIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        constructWriters(outputPrefix.getPath(), isZipped);
    }

    private void constructWriters(String outputPrefix, boolean isZipped) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        coreWriter = new GenericFile(outputPrefix + FileExtensions.IPCR + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
        barcodeWriter = new GenericFile(outputPrefix + FileExtensions.IPCR_BARCODES + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        // Write barcode
        barcodeWriter.write(record.getBarcode());
        barcodeWriter.newLine();

        writeRecordToWriter(record);
    }

    @Override
    public void writeHeader() throws IOException {
        this.writeHeader("");
    }

    @Override
    public void writeHeader(String reason) throws IOException {
        writeHeaderToWriter(reason);
    }

    @Override
    public String[] getBarcodeCountFilesSampleNames() {
        return barcodeCountFilesSampleNames;
    }

    @Override
    public void setBarcodeCountFilesSampleNames(String[] barcodeCountFilesSampleNames) {
        this.barcodeCountFilesSampleNames = barcodeCountFilesSampleNames;
    }

    public void flushAndClose() throws IOException {
        barcodeWriter.flush();
        barcodeWriter.close();
        coreWriter.flush();
        coreWriter.close();
    }

    // Split so can be used by subclass
    protected void writeHeaderToWriter(String reason) throws IOException {
        write("barcode");
        write(sep);
        write("readName");
        write(sep);
        write("chromosome");
        write(sep);
        write("readOneStart");
        write(sep);
        write("readOneEnd");
        write(sep);
        write("readTwoStart");
        write(sep);
        write("readTwoEnd");
        write(sep);
        write("readOneFlag");
        write(sep);
        write("readTwoFlag");
        write(sep);
        write("readOneMQ");
        write(sep);
        write("readTwoMQ");
        write(sep);
        write("readOneCigar");
        write(sep);
        write("readTwoCigar");
        write(sep);
        write("readOneStrand");
        write(sep);
        write("readTwoStrand");
        write(sep);
        write("ipcrCount");
        write(sep);
        if (barcodeCountFilesSampleNames != null) {
            for (String key : barcodeCountFilesSampleNames) {
                int idx = key.indexOf('.');
                if (idx < 0) {
                    write(key);
                } else {
                    write(key.substring(0, idx));
                }
                write(sep);
            }
        }

        writeNewLine();
    }

    // Split so can be used bu subclass
    protected void writeRecordToWriter(IpcrRecord record) throws IOException {
        // Alignment info
        write(record.getBarcode());
        write(sep);
        write(record.getQueryReadName());
        write(sep);
        write(record.getContig());
        write(sep);

        write(Integer.toString(record.getQueryStart()));
        write(sep);
        write(Integer.toString(record.getQueryEnd()));
        write(sep);

        write(Integer.toString(record.getMateStart()));
        write(sep);
        write(Integer.toString(record.getMateEnd()));
        write(sep);

        write(Integer.toString(record.getQuerySamFlags()));
        write(sep);
        write(Integer.toString(record.getMateSamFlags()));
        write(sep);

        write(Integer.toString(record.getQueryMappingQuality()));
        write(sep);
        write(Integer.toString(record.getMateMappingQuality()));
        write(sep);

        write(record.getQuerySigar());
        write(sep);
        write(record.getMateCigar());
        write(sep);

        write(Character.toString(record.getQueryStrand()));
        write(sep);

        write(Character.toString(record.getMateStrand()));
        write(sep);

        write(Integer.toString(record.getIpcrDuplicateCount()));
        write(sep);


        if (barcodeCountFilesSampleNames != null) {
            for (String key : barcodeCountFilesSampleNames) {
                write(Integer.toString(record.getBarcodeCountPerSample().get(key)));
                write(sep);
            }
        }

        writeNewLine();
    }

    protected void write(String line) throws IOException {
        coreWriter.write(line);
    }

    protected void writeNewLine() throws IOException {
        coreWriter.newLine();
    }
}
