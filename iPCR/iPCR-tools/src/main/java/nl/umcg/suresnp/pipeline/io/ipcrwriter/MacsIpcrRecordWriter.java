package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.File;
import java.io.IOException;

public class MacsIpcrRecordWriter implements IpcrOutputWriter {
    private BedIpcrRecordWriter ipcrBedWriter;
    private BedIpcrRecordWriter cdnaBedWriter;

    public MacsIpcrRecordWriter(File outputPrefix, boolean isZipped) throws IOException {
        // Fixed writers
        ipcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".ipcr"), isZipped);
        cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".cdna"), isZipped);
        ipcrBedWriter.setSampleToWrite(null);
        //ipcrBedWriter.setSampleIndexToWrite(-1);
        //cdnaBedWriter.setSampleIndexToWrite(0);
    }

    public MacsIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleName) throws IOException {
        // Fixed writers
        ipcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".ipcr"), isZipped);
        cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".cdna"), isZipped, barcodeCountFilesSampleName);
        ipcrBedWriter.setSampleToWrite(null);

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

    @Override
    public void setSampleToWrite(String sample) {
        ipcrBedWriter.setSampleToWrite(null);
        cdnaBedWriter.setSampleToWrite(sample);
    }
}
