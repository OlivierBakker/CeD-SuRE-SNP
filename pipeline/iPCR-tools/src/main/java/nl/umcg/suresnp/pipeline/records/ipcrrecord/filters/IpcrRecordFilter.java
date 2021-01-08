package nl.umcg.suresnp.pipeline.records.ipcrrecord.filters;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

public interface IpcrRecordFilter {
    boolean passesFilter(IpcrRecord ipcrRecord);
    void invertFilter();
    String getFilterName();
    IpcrRecordFilterType getFilterType();
}
