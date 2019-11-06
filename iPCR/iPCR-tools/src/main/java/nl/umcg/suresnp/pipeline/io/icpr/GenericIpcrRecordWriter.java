package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class GenericIpcrRecordWriter implements IpcrOutputWriter {

    protected OutputStream outputStream;
    protected BufferedWriter writer;
    private String[] barcodeCountFilesSampleNames;
    private final String sep = "\t";

    public GenericIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix + ".gz"));
        }
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

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public GenericIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
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
        writer.write(record.getBarcode());
        writer.write(sep);
        writer.write(record.getReadName());
        writer.write(sep);
        writer.write(record.getContig());
        writer.write(sep);

        writer.write(Integer.toString(record.getPrimaryEnd()));
        writer.write(sep);
        writer.write(Integer.toString(record.getPrimaryEnd()));
        writer.write(sep);

        writer.write(Integer.toString(record.getMateStart()));
        writer.write(sep);
        writer.write(Integer.toString(record.getMateEnd()));
        writer.write(sep);

        writer.write(Integer.toString(record.getPrimarySamFlags()));
        writer.write(sep);
        writer.write(Integer.toString(record.getMateSamFlags()));
        writer.write(sep);

        writer.write(Integer.toString(record.getPrimaryMappingQuality()));
        writer.write(sep);
        writer.write(Integer.toString(record.getMateMappingQuality()));
        writer.write(sep);

        writer.write(record.getPrimaryCigar());
        writer.write(sep);
        writer.write(record.getMateCigar());
        writer.write(sep);

        writer.write(record.getPrimaryStrand());
        writer.write(sep);

        writer.write(record.getMateStrand());
        writer.write(sep);

        if (record.getBarcodeCountPerSample() != null) {
            for (String key: barcodeCountFilesSampleNames) {
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
        writer.write("barcode");
        writer.write(sep);
        writer.write("readName");
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
        writer.write(sep);
        if (barcodeCountFilesSampleNames != null) {
            for (String key: barcodeCountFilesSampleNames) {
                int idx = key.indexOf('.');
                writer.write(key.substring(0, idx));
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
