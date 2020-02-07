package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificSamBasedIpcrRecord;

import java.io.IOException;

public class GenericAlleleSpecificIpcrRecordStdoutWriter implements AlleleSpecificIpcrOutputWriter {

    public GenericAlleleSpecificIpcrRecordStdoutWriter() {
    }

    @Override
    public void writeRecord(AlleleSpecificSamBasedIpcrRecord record) throws IOException {

        System.out.println(record.getBarcode() + "\t"
                + record.getPrimarySamRecord().getReadName() + "\t"
                + record.getGeneticVariant().getPrimaryVariantId());
    }

    @Override
    public void writeRecord(AlleleSpecificSamBasedIpcrRecord record, String reason) throws IOException {

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
