package nl.umcg.suresnp.pipeline.records.inforecord.filters;

import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;

public class FivePrimeFragmentLengthEqualsFilter implements InfoRecordFilter {

    private final String filtername="FivePrimeFragmentLengthEqualsFilter";
    private int barcodeLength;

    public FivePrimeFragmentLengthEqualsFilter(int barcodeLength) {
        this.barcodeLength = barcodeLength;
    }

    @Override
    public boolean passesFilter(InfoRecord infoRecord) {
        return infoRecord.getBarcode().length() == this.barcodeLength;
    }

    @Override
    public String getFilterName() {
        return filtername;
    }

}
