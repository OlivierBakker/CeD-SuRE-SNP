package nl.umcg.suresnp.pipeline.records.inforecords.consumers;


import nl.umcg.suresnp.pipeline.records.inforecords.InfoRecord;

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
