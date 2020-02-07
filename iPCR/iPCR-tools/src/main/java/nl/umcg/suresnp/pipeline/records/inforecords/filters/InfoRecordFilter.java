package nl.umcg.suresnp.pipeline.records.inforecords.filters;

import nl.umcg.suresnp.pipeline.records.inforecords.InfoRecord;

public interface InfoRecordFilter {

    boolean passesFilter(InfoRecord infoRecord);
    String getFilterName();
}
