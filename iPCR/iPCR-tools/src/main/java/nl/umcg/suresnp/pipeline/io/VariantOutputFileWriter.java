package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.AnnotatedIpcrRecord;
import nl.umcg.suresnp.pipeline.VariantType;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class VariantOutputFileWriter {
    private OutputStream outputStream;
    private BufferedWriter writer;

    public VariantOutputFileWriter(File outputPrefix, boolean isZipped) throws IOException {

        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public void writeIPCRRecord(AnnotatedIpcrRecord record) throws IOException {

        String sep = "\t";
        for(GeneticVariant variant :record.getOverlappingVariants()) {
            // Determine the length of ref and alt alleles, used for determining insert vs deletion.
            // This works only for bi-allelic variants, if multi allelic the first is taken.
            int refLength = variant.getRefAllele().toString().length();
            int altLength = variant.getAlternativeAlleles().getAllelesAsString().get(0).length();
            String allele = record.getBaseAt(variant.getStartPos(), variant.getStartPos() + ((altLength + refLength) - 1));
            VariantType variantType = record.checkGeneticVariantAlleles(variant);

            String varId = variant.getSequenceName() + ":" + variant.getStartPos() + ":" + variant.getRefAllele().toString() + ":" + String.join("|", variant.getAlternativeAlleles().getAllelesAsString());
            String rsId = variant.getPrimaryVariantId();
            String varType = "null";

            if (rsId == null) {
                rsId = "null";
            }

            switch (variantType) {
                case SNP:
                    varType = "SNP";
                    break;
                case INSERTION:
                    // Insertion, determine if its the inserted or reference allele
                    if (!allele.equals(variant.getAlternativeAlleles().getAllelesAsString().get(0))) {
                        allele = allele.substring(0, 1);
                    }
                    varType = "INS";
                    break;
                case DELETION:
                    // Deletion
                    if (!allele.equals(variant.getRefAllele().toString())) {
                        allele = allele.substring(0, 1);
                    }
                    varType = "DEL";
                    break;
                case INVALID:
                    // These should have been filtered out already, but it doesn't hurt to check
                    continue;
                case UNDETERMINED:
                    // These should have been filtered out already, but it doesn't hurt to check
                    continue;
            }

            writer.write(record.getBarcode());
            writer.write(sep);
            writer.write(varId);
            writer.write(sep);
            writer.write(rsId);
            writer.write(sep);
            writer.write(varType);
            writer.write(sep);
            writer.write(variant.getSequenceName());
            writer.write(sep);
            writer.write(Integer.toString(variant.getStartPos()));
            writer.write(sep);
            writer.write(variant.getRefAllele().toString());
            writer.write(sep);
            writer.write(String.join("|", variant.getAlternativeAlleles().getAllelesAsString()));
            writer.write(sep);
            writer.write(allele);
            writer.newLine();
        }
    }

    public void close() throws IOException {
        writer.flush();
        writer.close();
        outputStream.close();
    }

}
