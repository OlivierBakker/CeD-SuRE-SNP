package nl.umcg.suresnp.pipeline.inforecords.consumers;

import nl.umcg.suresnp.pipeline.inforecords.InfoRecord;

import java.util.HashMap;
import java.util.Map;

public class StringMapInfoRecordConsumer implements InfoRecordConsumer {

    private Map<String, String> output;

    public StringMapInfoRecordConsumer() {
        this.output = new HashMap<>();
    }

    @Override
    public void proccesInfoRecord(InfoRecord record) {
        output.put(record.getReadId(), record.getBarcode());
    }

    public Map<String, String> getOutput() {
        return output;
    }
}
