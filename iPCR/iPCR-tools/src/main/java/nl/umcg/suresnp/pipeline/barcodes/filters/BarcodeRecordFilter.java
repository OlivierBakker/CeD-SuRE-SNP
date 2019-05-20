package nl.umcg.suresnp.pipeline.barcodes.filters;

import nl.umcg.suresnp.pipeline.barcodes.BarcodeRecord;

public interface BarcodeRecordFilter {

    boolean passesFilter(BarcodeRecord barcodeRecord);
}
