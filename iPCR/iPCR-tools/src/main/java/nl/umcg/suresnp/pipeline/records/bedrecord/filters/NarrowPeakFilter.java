package nl.umcg.suresnp.pipeline.records.bedrecord.filters;

import nl.umcg.suresnp.pipeline.io.bedreader.NarrowPeakReader;
import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;
import nl.umcg.suresnp.pipeline.records.summarystatistic.SummaryStatisticRecord;
import nl.umcg.suresnp.pipeline.records.summarystatistic.filters.FilterType;

public interface NarrowPeakFilter {
    /**
     * Passes filter boolean.
     *
     * @param record the record
     * @return the boolean
     */
    boolean passesFilter(NarrowPeakRecord record);

    /**
     * Gets filter type.
     *
     * @return the filter type
     */
    String getFilterType();
}
