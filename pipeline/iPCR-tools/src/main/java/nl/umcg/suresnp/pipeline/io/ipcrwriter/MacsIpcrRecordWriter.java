package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.SampleSumScoreProvider;
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

    public MacsIpcrRecordWriter(File outputPrefix, boolean isZipped, AdaptableScoreProvider scoreProvider, boolean writeIpcr) throws IOException {
        this.isZipped = isZipped;
        this.outputPrefix = outputPrefix;
        this.writeIpcr = writeIpcr;

        // Fixed writers
        if (writeIpcr) {
            ipcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix.getPath() + FileExtensions.MACS_IPCR), isZipped, new SampleSumScoreProvider(new String[]{"IPCR"}));
        }

        cdnaBedWriters = new HashMap<>();
        if (scoreProvider != null) {
            BedIpcrRecordWriter cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix.getPath() + FileExtensions.MACS_CDNA), isZipped, scoreProvider);
            cdnaBedWriters.put(scoreProvider.getSamplesAsString(), cdnaBedWriter);
        } else {
            LOGGER.info("Initializing writer with no cDNA samples");
        }
    }

    public MacsIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleName, AdaptableScoreProvider scoreProvider, boolean writeIpcr) throws IOException {
        this.isZipped = isZipped;
        this.outputPrefix = outputPrefix;
        this.writeIpcr = writeIpcr;
        this.barcodeCountFilesSampleNames = barcodeCountFilesSampleName;

        // Fixed writers
        if (writeIpcr) {
            ipcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix.getPath() + FileExtensions.MACS_IPCR), isZipped, new SampleSumScoreProvider(new String[]{"IPCR"}));
        }

        cdnaBedWriters = new HashMap<>();
        if (scoreProvider != null) {
            BedIpcrRecordWriter cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix.getPath() + FileExtensions.MACS_CDNA), isZipped, scoreProvider);
            cdnaBedWriters.put(scoreProvider.getSamplesAsString(), cdnaBedWriter);
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
            for (String sample : getBarcodeCountFilesSampleNames()) {
                if (!cdnaBedWriters.containsKey(sample)) {
                    // If it does not exist, add a new writer for the sample
                    BedIpcrRecordWriter writer = new BedIpcrRecordWriter(new File(outputPrefix + "_" + sample + FileExtensions.MACS_CDNA), isZipped,
                            new SampleSumScoreProvider(new String[]{sample}));
                    cdnaBedWriters.put(sample, writer);
                } else {
                    // If it does exist, update with new samples
                    cdnaBedWriters.get(sample).setBarcodeCountFilesSampleNames(getBarcodeCountFilesSampleNames());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
