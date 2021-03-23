package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificIpcrRecord;

import java.io.File;
import java.io.IOException;

public class MinimalAlleleSpecificIpcrRecordWriter extends AlleleSpecificIpcrRecordWriter implements AlleleSpecificIpcrOutputWriter {

    private final String sep = "\t";

    public MinimalAlleleSpecificIpcrRecordWriter(File outputPrefix, String[] barcodeCountFilesSampleNames, boolean isZipped) throws IOException {
        super(outputPrefix, barcodeCountFilesSampleNames, isZipped);
    }

    public MinimalAlleleSpecificIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        super(outputPrefix, isZipped);
    }

    @Override
    protected void writeHeaderToWriter(String reason) throws IOException {
        write("barcode");
        write(sep);
        write("variantId");
        write(sep);
        write("variantContig");
        write(sep);
        write("variantStart");
        write(sep);
        write("variantStartInRead");
        write(sep);
        write("alleleInRead");
        write(sep);
        write("orientation");
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
        write(record.getVariantIdentifier());
        write(sep);
        write(record.getGeneticVariant().getSequenceName());
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
        write(Character.toString(record.getOrientation()));
        write(sep);
        write(Integer.toString(record.getIpcrDuplicateCount()));
        if (getBarcodeCountFilesSampleNames() != null) {
            for (String key : getBarcodeCountFilesSampleNames()) {
                write(sep);
                write(Integer.toString(record.getBarcodeCountPerSample().get(key)));
            }
        }
        writeNewLine();
    }

}
