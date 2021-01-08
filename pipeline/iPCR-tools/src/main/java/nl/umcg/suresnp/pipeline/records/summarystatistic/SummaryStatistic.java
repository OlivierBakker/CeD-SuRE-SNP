package nl.umcg.suresnp.pipeline.records.summarystatistic;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.Locatable;
import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SummaryStatistic {

    private GenericFile path;
    private String name;
    private IntervalTreeMap<SummaryStatisticRecord> records;
    private Map<String, Interval> indexMap;

    public SummaryStatistic(GenericFile path, IntervalTreeMap<SummaryStatisticRecord> records) {
        this.path = path;
        this.name = path.getBaseName();
        this.records = records;
        this.indexMap = new HashMap<>();

        for (SummaryStatisticRecord curRecord : this.records.values()) {
            indexMap.put(curRecord.getPrimaryVariantId(), new Interval(curRecord.getContig(), curRecord.getStart(), curRecord.getEnd() + 1));
        }
    }

    public GenericFile getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get a variant based on the variant id
     *
     * @param key
     * @return
     */
    public SummaryStatisticRecord queryVariant(String key) {

        if (indexMap.containsKey(key)) {
            Collection<SummaryStatisticRecord> tmp = this.query(indexMap.get(key));
            for (SummaryStatisticRecord curRecord : tmp) {
                if (curRecord.getPrimaryVariantId().equals(key)) {
                    return curRecord;
                }
            }
        }
        return null;
    }


    /**
     * Get a variant based on the variant id
     *
     * @param key
     * @return
     */
    public SummaryStatisticRecord queryVariant(GeneticVariantInterval key) {
        Collection<SummaryStatisticRecord> tmp = this.query(key);
        for (SummaryStatisticRecord curRecord : tmp) {
            if (curRecord.getPrimaryVariantId().equals(key.getPrimaryVariantId())) {
                return curRecord;
            }
        }

        return null;
    }

    /**
     * Get a set of variants based on genomic position
     *
     * @param key
     * @return
     */
    public Collection<SummaryStatisticRecord> query(Locatable key) {
        return records.getOverlapping(key);
    }

}
