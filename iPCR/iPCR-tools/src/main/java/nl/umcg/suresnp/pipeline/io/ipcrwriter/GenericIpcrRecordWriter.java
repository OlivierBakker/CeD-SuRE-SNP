package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class GenericIpcrRecordWriter implements IpcrOutputWriter {

    private BufferedWriter barcodeWriter;
    private BufferedWriter coreWriter;
    private String[] barcodeCountFilesSampleNames;
    private final String sep = "\t";

    public GenericIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames) throws IOException {

        String suffix = "";
        if (isZipped) {
            suffix = ".gz";
        }
        coreWriter = new GenericFile(outputPrefix + ".ipcr" + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
        barcodeWriter = new GenericFile(outputPrefix + ".ipcr.barcodes" + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();


        if (barcodeCountFilesSampleNames != null) {
            this.barcodeCountFilesSampleNames = new String[barcodeCountFilesSampleNames.length];

            // Clean filenames, trim all .
            int i = 0;
            for (String curFile: barcodeCountFilesSampleNames) {
                String tmp = new GenericFile(curFile).getBaseName();
                //int idx = tmp.indexOf('.');
                //this.barcodeCountFilesSampleNames[i] = tmp.substring(0, idx);
                this.barcodeCountFilesSampleNames[i] = tmp;
                i++;
            }
        }

    }

    public GenericIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        String suffix = "";
        if (isZipped) {
            suffix = ".gz";
        }
        coreWriter = new GenericFile(outputPrefix + ".ipcr" + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
        barcodeWriter = new GenericFile(outputPrefix + ".ipcr.barcodes" + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
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

        // Alignment info
        coreWriter.write(record.getBarcode());
        coreWriter.write(sep);
        coreWriter.write(record.getPrimaryReadName());
        coreWriter.write(sep);
        coreWriter.write(record.getContig());
        coreWriter.write(sep);

        coreWriter.write(Integer.toString(record.getPrimaryStart()));
        coreWriter.write(sep);
        coreWriter.write(Integer.toString(record.getPrimaryEnd()));
        coreWriter.write(sep);

        coreWriter.write(Integer.toString(record.getMateStart()));
        coreWriter.write(sep);
        coreWriter.write(Integer.toString(record.getMateEnd()));
        coreWriter.write(sep);

        coreWriter.write(Integer.toString(record.getPrimarySamFlags()));
        coreWriter.write(sep);
        coreWriter.write(Integer.toString(record.getMateSamFlags()));
        coreWriter.write(sep);

        coreWriter.write(Integer.toString(record.getPrimaryMappingQuality()));
        coreWriter.write(sep);
        coreWriter.write(Integer.toString(record.getMateMappingQuality()));
        coreWriter.write(sep);

        coreWriter.write(record.getPrimaryCigar());
        coreWriter.write(sep);
        coreWriter.write(record.getMateCigar());
        coreWriter.write(sep);

        coreWriter.write(record.getPrimaryStrand());
        coreWriter.write(sep);

        coreWriter.write(record.getMateStrand());
        coreWriter.write(sep);

        coreWriter.write(Integer.toString(record.getIpcrDuplicateCount()));
        coreWriter.write(sep);

        if (barcodeCountFilesSampleNames != null) {
            for (String key: barcodeCountFilesSampleNames) {
                coreWriter.write(Integer.toString(record.getBarcodeCountPerSample().get(key)));
                coreWriter.write(sep);
            }
        }

        coreWriter.newLine();
    }

    @Override
    public void writeHeader() throws IOException {
        this.writeHeader("");
    }

    @Override
    public void writeHeader(String reason) throws IOException {
        coreWriter.write("barcode");
        coreWriter.write(sep);
        coreWriter.write("readName");
        coreWriter.write(sep);
        coreWriter.write("chromosome");
        coreWriter.write(sep);
        coreWriter.write("readOneStart");
        coreWriter.write(sep);
        coreWriter.write("readOneEnd");
        coreWriter.write(sep);
        coreWriter.write("readTwoStart");
        coreWriter.write(sep);
        coreWriter.write("readTwoEnd");
        coreWriter.write(sep);
        coreWriter.write("readOneFlag");
        coreWriter.write(sep);
        coreWriter.write("readTwoFlag");
        coreWriter.write(sep);
        coreWriter.write("readOneMQ");
        coreWriter.write(sep);
        coreWriter.write("readTwoMQ");
        coreWriter.write(sep);
        coreWriter.write("readOneCigar");
        coreWriter.write(sep);
        coreWriter.write("readTwoCigar");
        coreWriter.write(sep);
        coreWriter.write("readOneStrand");
        coreWriter.write(sep);
        coreWriter.write("readTwoStrand");
        coreWriter.write(sep);
        coreWriter.write("ipcrCount");
        coreWriter.write(sep);
        if (barcodeCountFilesSampleNames != null) {
            for (String key: barcodeCountFilesSampleNames) {
                int idx = key.indexOf('.');
                if (idx < 0) {
                    coreWriter.write(key);
                } else {
                    coreWriter.write(key.substring(0, idx));
                }
                coreWriter.write(sep);
            }
        }

        coreWriter.newLine();
    }

    public void flushAndClose() throws IOException {
        barcodeWriter.flush();
        barcodeWriter.close();
        coreWriter.flush();
        coreWriter.close();
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
