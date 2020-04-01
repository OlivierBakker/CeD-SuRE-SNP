package nl.umcg.suresnp.pipeline.records.ipcrrecord.filters;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

public class AnyBarcodeCountSmallerEqualsFilter implements IpcrRecordFilter {

    private static final IpcrRecordFilterType type = IpcrRecordFilterType.ANY_BC_ST_EQ;
    private int barcodeCount;
    private boolean filterFailed;

    public AnyBarcodeCountSmallerEqualsFilter(int barcodeCount) {
        this.barcodeCount = barcodeCount;
        this.filterFailed = false;
    }

    @Override
    public boolean passesFilter(IpcrRecord ipcrRecord) {
        for (String key: ipcrRecord.getBarcodeCountPerSample().keySet()) {
            if(ipcrRecord.getBarcodeCountPerSample().get(key) <= barcodeCount) {
                return !filterFailed;
            }
        }
        return filterFailed;
    }

    @Override
    public String getFilterName() {
        return type.toString();
    }

    @Override
    public void invertFilter() {
        filterFailed = !filterFailed;
    }

    @Override
    public IpcrRecordFilterType getFilterType() {
        return type;
    }
}
