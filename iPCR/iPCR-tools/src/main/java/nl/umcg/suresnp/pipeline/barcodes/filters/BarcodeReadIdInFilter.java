package nl.umcg.suresnp.pipeline.barcodes.filters;

import nl.umcg.suresnp.pipeline.barcodes.BarcodeRecord;

import java.util.Set;

public class BarcodeReadIdInFilter implements BarcodeRecordFilter {

    private Set<String> availableIds;


    public BarcodeReadIdInFilter(Set<String> availableIds) {
        this.availableIds = availableIds;
    }


    @Override
    public boolean passesFilter(BarcodeRecord barcodeRecord) {
        return availableIds.contains(barcodeRecord.getReadId());
    }
}
