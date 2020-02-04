package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.*;
import java.util.zip.GZIPOutputStream;


@Deprecated
public class BedGraphIpcrRecordWriter implements IpcrOutputWriter {

    protected OutputStream outputStream;
    protected BufferedWriter writer;
    private String[] barcodeCountFilesSampleNames;
    private String sampleToWrite;

    private final String sep = "\t";

    public BedGraphIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleNames) throws IOException {

        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath()  + ".bedGraph" + suffix).getAsBufferedWriter();

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

    public BedGraphIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath()  + ".bedGraph" + suffix).getAsBufferedWriter();
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        // For each cDNA count write out the record once

        writeBedRecord(record);
       /* // Write the record for each cDNA count
        if (record.getBarcodeCountPerSample() != null) {
            int i = 0;
            int cDNAcount = record.getBarcodeCountPerSample().get(barcodeCountFilesSampleNames[0]);

            if (cDNAcount > 0) {
                while (i < cDNAcount) {
                    writeBedRecord(record);
                    i ++;
                };
            }

        } else {
            writeBedRecord(record);
        }*/
    }


    private void writeBedRecord(IpcrRecord record) throws IOException {
        // chrom
        writer.write(record.getContig());
        writer.write(sep);

        // Make the positions 0 based and half open
        // chromStart
        writer.write(Integer.toString(record.getOrientationIndependentStart()));
        writer.write(sep);

        // chromEnd
        writer.write(Integer.toString(record.getOrientationIndependentEnd()));
        writer.write(sep);

        // score
        if (record.getBarcodeCountPerSample() != null) {
/*            for (String key: record.getBarcodeCountPerSample().keySet()) {
                writer.write(Integer.toString(record.getBarcodeCountPerSample().get(key)));
                writer.write(";");
            }*/
            float tmp;
            if (record.getIpcrDuplicateCount() > 0) {
                tmp = (float) record.getBarcodeCountPerSample().get(barcodeCountFilesSampleNames[0]) / record.getIpcrDuplicateCount();

            } else {
                tmp = 0;
            }
            writer.write(Float.toString(tmp));
            writer.write(sep);
            writer.write(Integer.toString(record.getIpcrDuplicateCount()));
            writer.write(sep);
            writer.write(Integer.toString(record.getBarcodeCountPerSample().get(barcodeCountFilesSampleNames[0])));
        }


        writer.newLine();
    }

    @Override
    public void writeHeader() throws IOException {
        this.writeHeader("");
    }

    @Override
    public void writeHeader(String reason) throws IOException {
/*        writer.write("chr");
        writer.write(sep);
        writer.write("start");
        writer.write(sep);
        writer.write("end");
        writer.write(sep);
        writer.write("barcode");
        writer.write(sep);
        writer.write("readName");
        writer.write(sep);*/
/*
        if (barcodeCountFilesSampleNames != null) {
            for (String key: barcodeCountFilesSampleNames) {
                int idx = key.indexOf('.');
                writer.write(key.substring(0, idx));
                writer.write(sep);
            }
        }
*/

/*
        writer.newLine();
*/

    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
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


