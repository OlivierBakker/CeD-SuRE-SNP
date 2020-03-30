package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MacsIpcrRecordWriter implements IpcrOutputWriter {
    private static final Logger LOGGER = Logger.getLogger(MacsIpcrRecordWriter.class);

    private BedIpcrRecordWriter ipcrBedWriter;
    private Map<String, BedIpcrRecordWriter> cdnaBedWriters;
    private boolean writeIpcr;
    private boolean isZipped;
    private File outputPrefix;
    private String[] barcodeCountFilesSampleNames;
    private String sampleToWrite;

    public MacsIpcrRecordWriter(File outputPrefix, boolean isZipped, String sampleToWrite, boolean writeIpcr) throws IOException {
        this.isZipped = isZipped;
        this.outputPrefix = outputPrefix;
        this.writeIpcr = writeIpcr;
        this.sampleToWrite = sampleToWrite;

        // Fixed writers
        if (writeIpcr) {
            ipcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".ipcr"), isZipped);
            ipcrBedWriter.setSampleToWrite("IPCR");
        }

        cdnaBedWriters = new HashMap<>();
        if (sampleToWrite != null) {
            BedIpcrRecordWriter cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".cdna"), isZipped);
            cdnaBedWriter.setSampleToWrite(sampleToWrite);
            cdnaBedWriters.put(sampleToWrite, cdnaBedWriter);
        } else {
            LOGGER.info("Initializing writer with no cDNA samples");
        }
    }

    public MacsIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleName, String sampleToWrite, boolean writeIpcr) throws IOException {
        this.isZipped = isZipped;
        this.outputPrefix = outputPrefix;
        this.writeIpcr = writeIpcr;
        this.sampleToWrite = sampleToWrite;
        this.barcodeCountFilesSampleNames = barcodeCountFilesSampleName;

        // Fixed writers
        if (writeIpcr) {
            ipcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".ipcr"), isZipped);
            ipcrBedWriter.setSampleToWrite("IPCR");
        }

        cdnaBedWriters = new HashMap<>();
        if (sampleToWrite != null) {
            BedIpcrRecordWriter cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix + ".cdna"), isZipped);
            cdnaBedWriter.setSampleToWrite(sampleToWrite);
            cdnaBedWriters.put(sampleToWrite, cdnaBedWriter);
        } else {
            updateCdnaBedWriters();
        }
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        if (writeIpcr) {
            ipcrBedWriter.writeRecord(record, "");
        }

        for (BedIpcrRecordWriter cdnaWriter : cdnaBedWriters.values()) {
            cdnaWriter.writeRecord(record, "");
        }
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {
        if (writeIpcr) {
            ipcrBedWriter.writeRecord(record, reason);
        }
        for (BedIpcrRecordWriter cdnaWriter : cdnaBedWriters.values()) {
            cdnaWriter.writeRecord(record, "");
        }
    }

    @Override
    public void writeHeader() throws IOException {
        //ipcrBedWriter.writeHeader("");
        //cdnaBedWriter.writeHeader("");
    }

    @Override
    public void writeHeader(String reason) throws IOException {
        //ipcrBedWriter.writeHeader(reason);
        //cdnaBedWriter.writeHeader(reason);

    }

    @Override
    public void flushAndClose() throws IOException {
        if (writeIpcr) {
            ipcrBedWriter.flushAndClose();
        }
        for (BedIpcrRecordWriter cdnaWriter : cdnaBedWriters.values()) {
            cdnaWriter.flushAndClose();
        }
    }

    @Override
    public String[] getBarcodeCountFilesSampleNames() {
        return barcodeCountFilesSampleNames;
    }

    @Override
    public void setBarcodeCountFilesSampleNames(String[] barcodeCountFilesSampleNames) {
        this.barcodeCountFilesSampleNames = barcodeCountFilesSampleNames;
        updateCdnaBedWriters();
    }

    private void updateCdnaBedWriters() {
        try {
            if (sampleToWrite == null) {
                for (String sample : getBarcodeCountFilesSampleNames()) {
                    if (!cdnaBedWriters.containsKey(sample)) {
                        // If it does not exist, add a new writer for the sample
                        BedIpcrRecordWriter writer = new BedIpcrRecordWriter(new File(outputPrefix + "_" + sample + ".cdna"), isZipped, getBarcodeCountFilesSampleNames());
                        writer.setSampleToWrite(sample);
                        cdnaBedWriters.put(sample, writer);
                    } else {
                        // If it does exist, update with new samples
                        cdnaBedWriters.get(sample).setBarcodeCountFilesSampleNames(getBarcodeCountFilesSampleNames());
                    }
                }
            } else {
                cdnaBedWriters.get(sampleToWrite).setBarcodeCountFilesSampleNames(getBarcodeCountFilesSampleNames());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
