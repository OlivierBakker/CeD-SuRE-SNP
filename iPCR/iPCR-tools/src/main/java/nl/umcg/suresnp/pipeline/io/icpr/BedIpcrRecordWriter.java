package nl.umcg.suresnp.pipeline.io.icpr;

import htsjdk.samtools.SAMRecord;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.BamBasedIpcrRecord;
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
            //int idx = tmp.indexOf('.');
           // this.barcodeCountFilesSampleNames[i] = tmp.substring(0, idx);
            this.barcodeCountFilesSampleNames[i] = tmp;
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

        // For each cDNA count write out the record once
        // Flip arround so that the primary sam record is the first position
        SAMRecord tmp;
        if (record.getPrimarySamRecord().getReadNegativeStrandFlag()) {
            tmp = record.getPrimarySamRecord();
            record.setPrimarySamRecord(record.getPrimarySamRecordMate());
            record.setPrimarySamRecordMate(tmp);
        }

        // Write the record for each cDNA count
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
        }
    }


    private void writeBedRecord(BamBasedIpcrRecord record) throws IOException {
        // chrom
        writer.write(record.getPrimarySamRecord().getContig());
        writer.write(sep);

        // Make the positions 0 based and half open
        // chromStart
        writer.write(Integer.toString(record.getPrimarySamRecord().getAlignmentStart() - 1));
        writer.write(sep);

        // chromEnd
        writer.write(Integer.toString(record.getPrimarySamRecordMate().getAlignmentEnd()));
        writer.write(sep);

        // name
        writer.write(record.getBarcode() + "|" + record.getPrimarySamRecord().getReadName());
        writer.write(sep);

        // score
/*        if (record.getBarcodeCountPerSample() != null) {
            for (String key: record.getBarcodeCountPerSample().keySet()) {
                writer.write(Integer.toString(record.getBarcodeCountPerSample().get(key)));
                writer.write(";");
            }
        }*/
        writer.write(".");
        writer.write(sep);

        // Strand
        if (record.getPrimarySamRecord().getReadNegativeStrandFlag()) {
            writer.write("-");
        } else {
            writer.write("+");
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
