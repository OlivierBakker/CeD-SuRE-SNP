package nl.umcg.suresnp.pipeline.records.summarystatistic.filters;

import nl.umcg.suresnp.pipeline.records.summarystatistic.Locus;
import nl.umcg.suresnp.pipeline.records.summarystatistic.SummaryStatisticRecord;

/**
 * The type Region filter.
 */
public class RegionFilter implements SummaryStatisticsRecordFilter {

    private static FilterType filterType = FilterType.REGION_FILTER;
    private String sequenceName;
    private long upperBound;
    private long lowerBound;

    /**
     * Instantiates a new Region filter.
     *
     * @param locus the locus
     */
    public RegionFilter(Locus locus) {
        this.sequenceName = locus.getContig();
        this.upperBound = locus.getEnd();
        this.lowerBound = locus.getStart();
    }

    /**
     * Instantiates a new Region filter.
     *
     * @param sequenceName the sequence name
     * @param upperBound   the upper bound
     * @param lowerBound   the lower bound
     */
    public RegionFilter(String sequenceName, long upperBound, long lowerBound) {
        this.sequenceName = sequenceName;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    @Override
    public boolean passesFilter(SummaryStatisticRecord record) {
        if (record.getContig().equals(sequenceName)) {
            return record.getPosition() >= lowerBound && record.getPosition() <= upperBound;
        } else {
            return false;
        }
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }
}
