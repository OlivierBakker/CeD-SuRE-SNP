package nl.umcg.suresnp.pipeline.records.bedrecord.filters;

import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;
import nl.umcg.suresnp.pipeline.records.summarystatistic.filters.FilterType;

public class ScoreGtThanFilter implements NarrowPeakFilter{

    private double score;

    public ScoreGtThanFilter(double score) {
        this.score = score;
    }

    @Override
    public boolean passesFilter(NarrowPeakRecord record) {
        return record.getScore() > score;
    }

    @Override
    public String getFilterType() {
        return "ScoreGtThanFilter";
    }
}
