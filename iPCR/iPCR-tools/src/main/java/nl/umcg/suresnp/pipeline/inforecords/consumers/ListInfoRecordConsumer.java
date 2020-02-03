package nl.umcg.suresnp.pipeline.inforecords.consumers;

import nl.umcg.suresnp.pipeline.inforecords.InfoRecord;
import org.apache.commons.collections4.list.TreeList;

import java.util.List;

public class ListInfoRecordConsumer implements InfoRecordConsumer {

    private List<InfoRecord> output;

    public ListInfoRecordConsumer() {
        this.output = new TreeList<>();
    }

    @Override
    public void proccesInfoRecord(InfoRecord record) {

    }


    public List<InfoRecord> getOutput() {
        return output;
    }
}
