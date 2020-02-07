package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.records.ipcrrecords.IpcrRecord;

import java.io.File;
import java.io.IOException;

public class MacsIpcrRecordWriter implements IpcrOutputWriter {
    private BedIpcrRecordWriter ipcrBedWriter;
    private BedIpcrRecordWriter cdnaBedWriter;

    public MacsIpcrRecordWriter(File outputPrefix, boolean isZipped, String sampleToWrite) throws IOException {
        // Fixed writers
        ipcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".ipcr"), isZipped);
        cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".cdna"), isZipped);
        ipcrBedWriter.setSampleToWrite("IPCR");
        cdnaBedWriter.setSampleToWrite(sampleToWrite);
        //ipcrBedWriter.setSampleIndexToWrite(-1);
        //cdnaBedWriter.setSampleIndexToWrite(0);
    }

    public MacsIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleName, String sampleToWrite) throws IOException {
        // Fixed writers
        ipcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".ipcr"), isZipped);
        cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".cdna"), isZipped, barcodeCountFilesSampleName);
        ipcrBedWriter.setSampleToWrite("IPCR");
        cdnaBedWriter.setSampleToWrite(sampleToWrite);
        //TODO: Hardcoded to first sample
        //ipcrBedWriter.setSampleIndexToWrite(-1);
        //cdnaBedWriter.setSampleIndexToWrite(0);
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        ipcrBedWriter.writeRecord(record, "");
        cdnaBedWriter.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        ipcrBedWriter.writeRecord(record, reason);
        cdnaBedWriter.writeRecord(record, reason);
    }

    @Override
    public void writeHeader() throws IOException {
        ipcrBedWriter.writeHeader("");
        cdnaBedWriter.writeHeader("");
    }

    @Override
    public void writeHeader(String reason) throws IOException {
        ipcrBedWriter.writeHeader(reason);
        cdnaBedWriter.writeHeader(reason);

    }

    @Override
    public void flushAndClose() throws IOException {
        ipcrBedWriter.flushAndClose();
        cdnaBedWriter.flushAndClose();
    }

    @Override
    public String[] getBarcodeCountFilesSampleNames() {
        return cdnaBedWriter.getBarcodeCountFilesSampleNames();
    }

    @Override
    public void setBarcodeCountFilesSampleNames(String[] barcodeCountFilesSampleNames) {
        cdnaBedWriter.setBarcodeCountFilesSampleNames(barcodeCountFilesSampleNames);
    }

    public void setSampleToWrite(String sample) {
        ipcrBedWriter.setSampleToWrite("IPCR");
        cdnaBedWriter.setSampleToWrite(sample);
    }
}
