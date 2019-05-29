package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.ipcrrecords.AnnotatedIpcrRecordWithMate;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.VariantType;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class DiscaredIpcrRecordWriter implements IpcrOutputWriter{
    private OutputStream outputStream;
    private BufferedWriter writer;
    private final String sep = "\t";


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
        // Ref allele
        writer.write(record.getGeneticVariant().getRefAllele().getAlleleAsString());
        List<String> alleles = record.getGeneticVariant().getVariantAlleles().getAllelesAsString();
        writer.write(sep);
        // Effect allele (dosage 2)
        writer.write(alleles.get(0));
        writer.write(sep);
        // Alt allele (dosage 0)
        writer.write(alleles.get(1));
        writer.write(sep);

        if (record.getReadAllele() == null) {
            writer.write(".");
        } else {
            writer.write(record.getReadAllele());
        }
        writer.write(sep);


/*
        if (record.getAlternativeAllele() == null) {
            writer.write(".");
        } else {
            writer.write(record.getAlternativeAllele());
        }
*/

        writer.write(sep);
        writer.write(record.getRecord().getCigarString());
        writer.write(sep);
        if (record.getRecord().getReadNegativeStrandFlag()) {
            writer.write("-");
        } else {
            writer.write("+");
        }
        writer.newLine();

    }

    @Override
    public void writeHeader() throws IOException {
        writer.write("readName");
        writer.write(sep);
        writer.write("variantId");
        writer.write(sep);
        writer.write("variantType");
        writer.write(sep);
        writer.write("refAllele");
        writer.write(sep);
        writer.write("dosg2Allele");
        writer.write(sep);
        writer.write("dosg0Allele");
        writer.write(sep);
        writer.write("alleleInRead");
        writer.write(sep);
        writer.write("cigarString");
        writer.write(sep);
        writer.write("strand");
        writer.newLine();
    }


    @Override
    public void writeHeader(String reason) throws IOException {
        writer.write(reason);
        writer.write(sep);
        writeHeader();

    }

    @Override
    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }

}
