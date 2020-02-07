package nl.umcg.suresnp.pipeline.records.ipcrrecords.filters;

import nl.umcg.suresnp.pipeline.records.ipcrrecords.IpcrRecord;

public interface IpcrRecordFilter {
    boolean passesFilter(IpcrRecord ipcrRecord);
    String getFilterName();
}
