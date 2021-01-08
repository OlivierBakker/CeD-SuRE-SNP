package nl.umcg.suresnp.pipeline.records.inforecord.filters;

import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;

import java.util.Set;

public class BarcodeContainedInFilter implements InfoRecordFilter {

    private final String filtername = "BarcodeContainedInFilter";
    private Set<String> pool;
    private boolean invert;

    public BarcodeContainedInFilter(Set<String> pool) {
        this.pool = pool;
        this.invert = false;
    }

    public BarcodeContainedInFilter(Set<String> pool, boolean invert) {
        this.pool = pool;
        this.invert = invert;
    }

    @Override
    public boolean passesFilter(InfoRecord infoRecord) {

        if (invert) {
            return !pool.contains(infoRecord.getBarcode());
        } else {
            return pool.contains(infoRecord.getBarcode());

        }
    }

    @Override
    public String getFilterName() {
        return filtername;
    }
}
