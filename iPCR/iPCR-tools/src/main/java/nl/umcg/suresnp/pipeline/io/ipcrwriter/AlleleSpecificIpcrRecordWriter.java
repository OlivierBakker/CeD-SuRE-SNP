package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificIpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.SamBasedAlleleSpecificIpcrRecord;
import org.molgenis.genotype.Allele;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class AlleleSpecificIpcrRecordWriter implements AlleleSpecificIpcrOutputWriter {

    private OutputStream outputStream;
    private BufferedWriter writer;
    private final String sep = "\t";
    private String[] barcodeCountFilesSampleNames;

    public AlleleSpecificIpcrRecordWriter(File outputPrefix, String[] barcodeCountFilesSampleNames, boolean isZipped) throws IOException {
        this.barcodeCountFilesSampleNames = barcodeCountFilesSampleNames;
        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix + ".gz"));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public AlleleSpecificIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        this.barcodeCountFilesSampleNames = null;
        if (!isZipped) {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputPrefix));
        } else {
            outputStream = new GZIPOutputStream(new FileOutputStream(outputPrefix + ".gz"));
        }

        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    @Override
    public void writeRecord(AlleleSpecificIpcrRecord record) throws IOException {
        writeRecord(record);
    }

    @Override
    public void writeRecord(AlleleSpecificIpcrRecord record, String reason) throws IOException {
        writeRecordToWriter(record);
    }

    @Override
    public void writeHeader() throws IOException {
       writeHeader("");
    }

    @Override
    public void writeHeader(String reason) throws IOException {
        writeHeaderToWriter(reason);
    }

    @Override
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

    // Split so can be used by subclass
    protected void writeHeaderToWriter(String reason) throws IOException {
        write("barcode");
        write(sep);
        write("readName");
        write(sep);
        write("sequence");
        write(sep);
        write("alignmentStart");
        write(sep);
        write("alignmentEnd");
        write(sep);
        write("variantId");
        write(sep);
        write("variantType");
        write(sep);
        write("variantStart");
        write(sep);
        write("variantStartInRead");
        write(sep);
        write("refAllele");
        write(sep);
        write("dosg2Allele");
        write(sep);
        write("dosg0Allele");
        write(sep);
        write("alleleInRead");
        write(sep);
        write("cigarString");
        write(sep);
        write("strand");
        write(sep);
        write("sampleId");
        write(sep);
        write("ipcrCount");

        if (getBarcodeCountFilesSampleNames() != null) {
            for (String key : getBarcodeCountFilesSampleNames()) {
                write(sep);
                int idx = key.indexOf('.');
                if (idx < 0) {
                    write(key);
                } else {
                    write(key.substring(0, idx));
                }
            }
        }
        writeNewLine();
    }

    // Split so can be used bu subclass
    protected void writeRecordToWriter(AlleleSpecificIpcrRecord record) throws IOException {
        // Alignment info
        write(record.getBarcode());
        write(sep);
        write(record.getPrimaryReadName());
        write(sep);
        write(record.getContig());
        write(sep);
        write(Integer.toString(record.getOrientationIndependentStart()));
        write(sep);
        write(Integer.toString(record.getOrientationIndependentEnd()));

        // Variant info
        write(sep);
        if (record.getGeneticVariant().getPrimaryVariantId() == null) {
            write(record.getGeneticVariant().getSequenceName()
                    + ":" + record.getGeneticVariant().getStartPos()
                    + "," + record.getGeneticVariant().getRefAllele().toString()
                    + "," + String.join("_", record.getGeneticVariant().getAlternativeAlleles().getAllelesAsString())
            );
        } else {
            write(record.getGeneticVariant().getPrimaryVariantId());
        }
        write(sep);
        write(record.getVariantType().toString());
        write(sep);
        write(Integer.toString(record.getGeneticVariant().getStartPos()));
        write(sep);
        write(Integer.toString(record.getVariantStartInRead()));
        write(sep);

        // Ref allele
        write(record.getGeneticVariant().getRefAllele().getAlleleAsString());
        List<String> alleles = record.getGeneticVariant().getVariantAlleles().getAllelesAsString();
        write(sep);
        // Effect allele (dosage 2)
        write(alleles.get(0));
        write(sep);
        // Alt allele (dosage 0)
        write(alleles.get(1));
        write(sep);

        if (record.getReadAllele() == null) {
            write(".");
        } else {
            write(record.getReadAllele());
        }
        write(sep);
        write(record.getPrimaryCigar());
        write(sep);
        write(Character.toString(record.getPrimaryStrand()));

        write(sep);
        if (record.getSampleId() != null) {
            write(record.getSampleId());
        } else {
            write(".");
        }

        write(sep);
        write(Integer.toString(record.getIpcrDuplicateCount()));
        write(sep);

        if (getBarcodeCountFilesSampleNames() != null) {
            for (String key : getBarcodeCountFilesSampleNames()) {
                write(Integer.toString(record.getBarcodeCountPerSample().get(key)));
                write(sep);
            }
        }


        writeNewLine();
    }

    protected void write(String line) throws IOException {
        writer.write(line);
    }

    protected void writeNewLine() throws IOException {
        writer.newLine();
    }
}
