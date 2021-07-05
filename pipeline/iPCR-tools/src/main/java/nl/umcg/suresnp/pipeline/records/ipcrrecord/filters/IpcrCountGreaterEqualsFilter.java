package nl.umcg.suresnp.pipeline.records.ipcrrecord.filters;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

public class IpcrCountGreaterEqualsFilter implements IpcrRecordFilter {

    private static final IpcrRecordFilterType type = IpcrRecordFilterType.IPCR_COUNT_GT_EQ;
    private boolean filterFailed;
    private int count;

    public IpcrCountGreaterEqualsFilter(int count, boolean filterFailed) {
        this.count = count;
        this.filterFailed = filterFailed;
    }

    @Override
    public boolean passesFilter(IpcrRecord ipcrRecord) {

        if (ipcrRecord.getIpcrDuplicateCount() >= count) {
            return !filterFailed;
        }

        return filterFailed;
    }

    @Override
    public void invertFilter() {
        filterFailed = !filterFailed;
    }

    @Override
    public String getFilterName() {
        return type.toString();
    }

    @Override
    public IpcrRecordFilterType getFilterType() {
        return type;
    }
}
