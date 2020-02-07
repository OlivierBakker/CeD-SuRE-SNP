package nl.umcg.suresnp.pipeline.records.inforecord.consumers;


import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;

import java.util.HashMap;
import java.util.Map;

public class MapInfoRecordConsumer implements InfoRecordConsumer {

    private Map<String, InfoRecord> output;

    public MapInfoRecordConsumer() {
        this.output = new HashMap<>();
    }

    @Override
    public void proccesInfoRecord(InfoRecord record) {

    }

    public Map<String, InfoRecord> getOutput() {
        return output;
    }
}
