package nl.umcg.suresnp.pipeline.records.ipcrrecord.filters;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

/**
 * Filter records where ANY BC count is >= x
 */
public class AnyBarcodeCountGreaterEqualsFilter implements IpcrRecordFilter {

    private static final IpcrRecordFilterType type = IpcrRecordFilterType.ANY_BC_GT_EQ;
    private int barcodeCount;
    private boolean filterFailed;

    public AnyBarcodeCountGreaterEqualsFilter(int barcodeCount) {
        this.barcodeCount = barcodeCount;
        this.filterFailed = false;
    }

    public AnyBarcodeCountGreaterEqualsFilter(int barcodeCount, boolean invert) {
        this.barcodeCount = barcodeCount;
        this.filterFailed = invert;
    }

    @Override
    public boolean passesFilter(IpcrRecord ipcrRecord) {
        for (String key: ipcrRecord.getBarcodeCountPerSample().keySet()) {
            if(ipcrRecord.getBarcodeCountPerSample().get(key) >= barcodeCount) {
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
