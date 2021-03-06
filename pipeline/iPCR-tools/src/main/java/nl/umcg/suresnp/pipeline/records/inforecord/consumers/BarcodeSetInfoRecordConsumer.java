package nl.umcg.suresnp.pipeline.records.inforecord.consumers;

import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;

import java.util.HashSet;
import java.util.Set;

public class BarcodeSetInfoRecordConsumer implements InfoRecordConsumer {

    private Set<String> output;

    public BarcodeSetInfoRecordConsumer() {
        this.output = new HashSet<>();
    }

    @Override
    public void proccesInfoRecord(InfoRecord record) {

    }

    public Set<String> getOutput() {
        return output;
    }
}
