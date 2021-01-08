package nl.umcg.suresnp.pipeline.records.inforecord.filters;

import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;

public interface InfoRecordFilter {

    boolean passesFilter(InfoRecord infoRecord);
    String getFilterName();
}
