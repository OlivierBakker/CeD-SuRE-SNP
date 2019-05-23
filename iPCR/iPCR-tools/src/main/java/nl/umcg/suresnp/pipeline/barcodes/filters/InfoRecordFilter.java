package nl.umcg.suresnp.pipeline.barcodes.filters;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;

public interface InfoRecordFilter {

    boolean passesFilter(InfoRecord infoRecord);
    String getFilterName();
}
