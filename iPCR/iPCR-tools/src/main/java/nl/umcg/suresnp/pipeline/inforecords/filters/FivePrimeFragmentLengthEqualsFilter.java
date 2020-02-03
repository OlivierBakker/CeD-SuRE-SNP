package nl.umcg.suresnp.pipeline.inforecords.filters;

import nl.umcg.suresnp.pipeline.inforecords.InfoRecord;

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