package nl.umcg.suresnp.pipeline.ipcrrecords.filters;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

public interface IpcrRecordFilter {
    boolean passesFilter(IpcrRecord ipcrRecord);
    String getFilterName();
}
