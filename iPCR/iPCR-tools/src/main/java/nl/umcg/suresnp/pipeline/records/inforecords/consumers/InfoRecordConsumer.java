package nl.umcg.suresnp.pipeline.records.inforecords.consumers;
import nl.umcg.suresnp.pipeline.records.inforecords.InfoRecord;

public interface InfoRecordConsumer {

    void proccesInfoRecord(InfoRecord record);

}
