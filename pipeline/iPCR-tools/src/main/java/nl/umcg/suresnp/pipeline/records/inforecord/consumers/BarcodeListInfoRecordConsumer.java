package nl.umcg.suresnp.pipeline.records.inforecord.consumers;


import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;

import java.util.ArrayList;
import java.util.List;

public class BarcodeListInfoRecordConsumer implements InfoRecordConsumer {

    private List<String> output;

    public BarcodeListInfoRecordConsumer() {
        this.output = new ArrayList<>();
    }

    @Override
    public void proccesInfoRecord(InfoRecord record) {
        output.add(record.getBarcode());
    }


    public List<String> getOutput() {
        return output;
    }
}
