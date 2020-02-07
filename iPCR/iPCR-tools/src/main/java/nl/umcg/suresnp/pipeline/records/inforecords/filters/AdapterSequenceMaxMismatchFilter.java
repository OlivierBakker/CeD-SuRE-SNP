package nl.umcg.suresnp.pipeline.records.inforecords.filters;

import nl.umcg.suresnp.pipeline.records.inforecords.InfoRecord;

public class AdapterSequenceMaxMismatchFilter implements InfoRecordFilter {

    private final String filtername = "AdapterSequenceMaxMismatchFilter";
    private int maxMismatches;

    public AdapterSequenceMaxMismatchFilter(int maxMismatches) {
        this.maxMismatches = maxMismatches;
    }

    @Override
    public boolean passesFilter(InfoRecord infoRecord) {
        return infoRecord.getBarcodeErrorCount() <= maxMismatches;
    }

    @Override
    public String getFilterName() {
        return filtername;
    }
}
