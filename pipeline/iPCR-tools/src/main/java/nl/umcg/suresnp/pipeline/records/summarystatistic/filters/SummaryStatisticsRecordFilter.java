package nl.umcg.suresnp.pipeline.records.summarystatistic.filters;

import nl.umcg.suresnp.pipeline.records.summarystatistic.SummaryStatisticRecord;

/**
 * The interface Summary statistics record filter.
 */
public interface SummaryStatisticsRecordFilter {

    /**
     * Passes filter boolean.
     *
     * @param record the record
     * @return the boolean
     */
    boolean passesFilter(SummaryStatisticRecord record);

    /**
     * Gets filter type.
     *
     * @return the filter type
     */
    FilterType getFilterType();
}
