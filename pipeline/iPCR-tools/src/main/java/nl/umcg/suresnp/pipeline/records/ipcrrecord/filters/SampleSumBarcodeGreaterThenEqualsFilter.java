package nl.umcg.suresnp.pipeline.records.ipcrrecord.filters;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.SampleSumScoreProvider;

public class SampleSumBarcodeGreaterThenEqualsFilter implements IpcrRecordFilter {

    private static final IpcrRecordFilterType type = IpcrRecordFilterType.SAMPLE_BC_GT_EQ;
    private int barcodeCount;
    private boolean filterFailed;
    private SampleSumScoreProvider provider;

    public SampleSumBarcodeGreaterThenEqualsFilter(int barcodeCount, String[] samples) {
        this.barcodeCount = barcodeCount;
        this.filterFailed = false;
        this.provider = new SampleSumScoreProvider(samples);
    }

    public SampleSumBarcodeGreaterThenEqualsFilter(int barcodeCount, String[] samples, boolean invert) {
        this.barcodeCount = barcodeCount;
        this.filterFailed = invert;
        this.provider = new SampleSumScoreProvider(samples);
    }

    @Override
    public boolean passesFilter(IpcrRecord ipcrRecord) {
        if (provider.getScore(ipcrRecord) >= barcodeCount) {
            return !filterFailed;
        }
        return filterFailed;
    }

    @Override
    public String getFilterName() {
        return type.toString();
    }

    @Override
    public void invertFilter() {
        filterFailed = !filterFailed;
    }

    @Override
    public IpcrRecordFilterType getFilterType() {
        return type;
    }
}
