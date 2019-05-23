package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.ipcrrecords.AnnotatedIpcrRecordWithMate;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.VariantType;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class DiscaredIpcrRecordWriter implements IpcrOutputWriter{
    private OutputStream outputStream;
    private BufferedWriter writer;

    public DiscaredIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        String sep = "\t";

        if (reason.length() > 1) {
            writer.write(reason);
            writer.write(sep);
        }


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


        if (record.getAlternativeAllele() == null) {
            writer.write(".");
        } else {
            writer.write(record.getAlternativeAllele());
        }

        writer.write(sep);
        writer.write(record.getRecord().getCigarString());
        writer.newLine();

    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }

}
