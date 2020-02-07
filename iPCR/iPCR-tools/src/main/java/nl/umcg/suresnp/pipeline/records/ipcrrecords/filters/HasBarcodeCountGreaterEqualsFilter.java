package nl.umcg.suresnp.pipeline.records.ipcrrecords.filters;

import nl.umcg.suresnp.pipeline.records.ipcrrecords.IpcrRecord;

public class HasBarcodeCountGreaterEqualsFilter implements IpcrRecordFilter {

    private int barcodeCount;

    public HasBarcodeCountGreaterEqualsFilter(int barcodeCount) {
        this.barcodeCount = barcodeCount;
    }

    @Override
    public boolean passesFilter(IpcrRecord ipcrRecord) {
        for (String key: ipcrRecord.getBarcodeCountPerSample().keySet()) {
            if(ipcrRecord.getBarcodeCountPerSample().get(key) >= barcodeCount) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getFilterName() {
        return "HasBarcodeCountGreaterEqualsFilter";
    }
}
