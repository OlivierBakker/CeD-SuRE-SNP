package nl.umcg.suresnp.pipeline.io.icpr;

import jdk.nashorn.internal.ir.annotations.Ignore;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecordWithMate;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class GenericIpcrRecordWriter implements IpcrOutputWriter {

    protected OutputStream outputStream;
    protected BufferedWriter writer;

    public GenericIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }


    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        String sep = "\t";

        writer.write(record.getBarcode());
        writer.write(sep);
        writer.write(record.getRecord().getReadName());
        writer.write(sep);

        if (record.getGeneticVariant().getPrimaryVariantId() == null) {
            writer.write(record.getGeneticVariant().getSequenceName()
                    + ":" + record.getGeneticVariant().getStartPos()
                    + "," + record.getGeneticVariant().getRefAllele().toString()
                    + "," + String.join("_", record.getGeneticVariant().getAlternativeAlleles().getAllelesAsString())
            );
        } else {
            writer.write(record.getGeneticVariant().getPrimaryVariantId());
        }
        writer.write(sep);
        writer.write(record.getVariantType().toString());
        writer.write(sep);
        writer.write(record.getGeneticVariant().getRefAllele().getAlleleAsString());
        writer.write(sep);
        writer.write(String.join("", record.getGeneticVariant().getAlternativeAlleles().getAllelesAsString()));
        writer.write(sep);

        if (record.getReadAllele() == null) {
            writer.write(".");
        } else {
            writer.write(record.getReadAllele());
        }
        writer.write(sep);

        writer.newLine();
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        writeRecord(record);
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }


}
