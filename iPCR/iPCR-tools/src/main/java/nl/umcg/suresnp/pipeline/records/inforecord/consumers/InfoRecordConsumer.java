package nl.umcg.suresnp.pipeline.records.inforecord.consumers;
import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;

public interface InfoRecordConsumer {

    void proccesInfoRecord(InfoRecord record);

}
