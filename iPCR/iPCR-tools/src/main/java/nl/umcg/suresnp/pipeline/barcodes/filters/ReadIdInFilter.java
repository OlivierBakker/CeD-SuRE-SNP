package nl.umcg.suresnp.pipeline.barcodes.filters;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;

import java.util.Set;

public class ReadIdInFilter implements InfoRecordFilter {

    private final String filtername="ReadIdInFilter";
    private Set<String> availableIds;


    public ReadIdInFilter(Set<String> availableIds) {
        this.availableIds = availableIds;
    }


    @Override
    public boolean passesFilter(InfoRecord infoRecord) {
        return availableIds.contains(infoRecord.getReadId());
    }

    @Override
    public String getFilterName() {
        return filtername;
    }
}
