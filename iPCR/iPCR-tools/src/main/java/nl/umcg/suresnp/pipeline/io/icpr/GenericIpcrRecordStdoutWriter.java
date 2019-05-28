package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecordWithMate;

import java.io.IOException;

public class GenericIpcrRecordStdoutWriter implements IpcrOutputWriter {

    public GenericIpcrRecordStdoutWriter() {
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {

        System.out.println(record.getBarcode() + "\t"
                + record.getRecord().getReadName() + "\t"
                + record.getGeneticVariant().getPrimaryVariantId());
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {

        writeRecord(record);
    }

    @Override
    public void flushAndClose() throws IOException {

    }
}
