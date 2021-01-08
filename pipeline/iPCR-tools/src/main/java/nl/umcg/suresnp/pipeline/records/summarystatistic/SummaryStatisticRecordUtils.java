package nl.umcg.suresnp.pipeline.records.summarystatistic;

import nl.umcg.suresnp.pipeline.records.summarystatistic.filters.SummaryStatisticsRecordFilter;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SummaryStatisticRecordUtils {

    private static final Logger LOGGER = Logger.getLogger(SummaryStatisticRecordUtils.class);

    /**
     * Filter summary statistics.
     *
     * @param records the records
     * @param filters the filters
     */
    public static void filterSummaryStatistics(Map<String, SummaryStatisticRecord> records, List<SummaryStatisticsRecordFilter> filters) {
        // Apply the specified filters to the set
        for (String curRecord : new HashSet<>(records.keySet())) {
            for (SummaryStatisticsRecordFilter filter : filters) {
                if (!filter.passesFilter(records.get(curRecord))) {
                    records.remove(curRecord);
                    break;
                }
            }
        }

        // LOGGER.info("Done filtering. " + records.size() + " records remaining");
    }
}
