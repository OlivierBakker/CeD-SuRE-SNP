package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificIpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.SamBasedAlleleSpecificIpcrRecord;

import java.io.IOException;

@Deprecated
public class GenericAlleleSpecificIpcrRecordStdoutWriter implements AlleleSpecificIpcrOutputWriter {

    public GenericAlleleSpecificIpcrRecordStdoutWriter() {
    }

    @Override
    public void writeRecord(AlleleSpecificIpcrRecord record) throws IOException {

        System.out.println(record.getBarcode() + "\t"
                + record.getPrimaryReadName() + "\t"
                + record.getGeneticVariant().getPrimaryVariantId());
    }

    @Override
    public void writeRecord(AlleleSpecificIpcrRecord record, String reason) throws IOException {
        writeRecord(record);
    }



    @Override
    public void writeHeader() throws IOException {

    }

    @Override
    public void writeHeader(String reason) throws IOException {

    }

    @Override
    public void flushAndClose() throws IOException {

    }
}
