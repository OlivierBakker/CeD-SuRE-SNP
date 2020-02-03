package nl.umcg.suresnp.pipeline.inforecords.consumers;

import nl.umcg.suresnp.pipeline.inforecords.InfoRecord;

public interface InfoRecordConsumer {

    void proccesInfoRecord(InfoRecord record);

}
