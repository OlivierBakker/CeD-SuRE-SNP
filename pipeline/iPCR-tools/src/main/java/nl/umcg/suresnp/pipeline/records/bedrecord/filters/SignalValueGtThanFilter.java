package nl.umcg.suresnp.pipeline.records.bedrecord.filters;

import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;

public class SignalValueGtThanFilter implements NarrowPeakFilter{

    private double signalValue;

    public SignalValueGtThanFilter(double signalValue) {
        this.signalValue = signalValue;
    }

    @Override
    public boolean passesFilter(NarrowPeakRecord record) {
        return record.getSignalValue() > signalValue;
    }

    @Override
    public String getFilterType() {
        return "SignalValueGtThanFilter";
    }
}
