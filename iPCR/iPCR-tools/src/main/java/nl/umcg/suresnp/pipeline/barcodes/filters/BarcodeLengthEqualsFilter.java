package nl.umcg.suresnp.pipeline.barcodes.filters;

import nl.umcg.suresnp.pipeline.barcodes.BarcodeRecord;

public class BarcodeLengthEqualsFilter implements BarcodeRecordFilter {

    private int barcodeLength;

    public BarcodeLengthEqualsFilter(int barcodeLength) {
        this.barcodeLength = barcodeLength;
    }

    @Override
    public boolean passesFilter(BarcodeRecord barcodeRecord) {
        return barcodeRecord.getBarcode().length() == this.barcodeLength;
    }
}
