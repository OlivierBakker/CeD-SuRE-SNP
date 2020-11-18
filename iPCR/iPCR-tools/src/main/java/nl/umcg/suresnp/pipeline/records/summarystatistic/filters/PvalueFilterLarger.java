package nl.umcg.suresnp.pipeline.records.summarystatistic.filters;

import nl.umcg.suresnp.pipeline.records.summarystatistic.SummaryStatisticRecord;

/**
 * The type Pvalue filter larger.
 */
public class PvalueFilterLarger implements SummaryStatisticsRecordFilter {

    private static FilterType filterType = FilterType.PVALUE_FILTER_GT;
    private double pvalueThreshold;

    /**
     * Instantiates a new Pvalue filter larger.
     *
     * @param pvalueThreshold the pvalue threshold
     */
    public PvalueFilterLarger(double pvalueThreshold) {
        this.pvalueThreshold = pvalueThreshold;
    }

    @Override
    public boolean passesFilter(SummaryStatisticRecord record) {

        try {
            return record.getPvalue() > pvalueThreshold;
        } catch (NullPointerException e) {
            throw new FieldNotAvailibleException("pvalue");
        }
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }
}
