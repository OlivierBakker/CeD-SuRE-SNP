package nl.umcg.suresnp.pipeline.inforecords.filters;

import nl.umcg.suresnp.pipeline.inforecords.InfoRecord;

public interface InfoRecordFilter {

    boolean passesFilter(InfoRecord infoRecord);
    String getFilterName();
}
