package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificIpcrRecord;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MinimalAlleleSpecificIpcrRecrodWriter extends AlleleSpecificIpcrRecordWriter implements AlleleSpecificIpcrOutputWriter {

    private final String sep = "\t";

    public MinimalAlleleSpecificIpcrRecrodWriter(File outputPrefix, String[] barcodeCountFilesSampleNames, boolean isZipped) throws IOException {
        super(outputPrefix, barcodeCountFilesSampleNames, isZipped);
    }

    public MinimalAlleleSpecificIpcrRecrodWriter(File outputPrefix, boolean isZipped) throws IOException {
        super(outputPrefix, isZipped);
    }

    @Override
    protected void writeHeaderToWriter(String reason) throws IOException {
        write("barcode");
        write(sep);
        write("variantId");
        write(sep);
        write("variantStart");
        write(sep);
        write("variantStartInRead");
        write(sep);
        write("alleleInRead");
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

    @Override
    protected void writeRecordToWriter(AlleleSpecificIpcrRecord record) throws IOException {
        // Alignment info
        write(record.getBarcode());
        write(sep);

        // Variant info
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
        write(Integer.toString(record.getGeneticVariant().getStartPos()));
        write(sep);
        write(Integer.toString(record.getVariantStartInRead()));
        write(sep);

        if (record.getReadAllele() == null) {
            write(".");
        } else {
            write(record.getReadAllele());
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

}
