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

public class StrandSpecificMacsIpcrRecordWriter implements IpcrOutputWriter {
    private static final Logger LOGGER = Logger.getLogger(StrandSpecificMacsIpcrRecordWriter.class);

    private BedIpcrRecordWriter plusIpcrBedWriter;
    private BedIpcrRecordWriter minusIpcrBedWriter;

    private Map<String, BedIpcrRecordWriter> plusCdnaBedWriters;
    private Map<String, BedIpcrRecordWriter> minusCdnaBedWriters;

    private boolean writeIpcr;
    private boolean isZipped;
    private File outputPrefix;
    private String[] barcodeCountFilesSampleNames;

    public StrandSpecificMacsIpcrRecordWriter(File outputPrefix, boolean isZipped, AdaptableScoreProvider scoreProvider, boolean writeIpcr) throws IOException {
        this(outputPrefix, isZipped, null, scoreProvider, writeIpcr);
    }

    public StrandSpecificMacsIpcrRecordWriter(File outputPrefix, boolean isZipped, String[] barcodeCountFilesSampleName, AdaptableScoreProvider scoreProvider, boolean writeIpcr) throws IOException {
        this.isZipped = isZipped;
        this.outputPrefix = outputPrefix;
        this.writeIpcr = writeIpcr;
        this.barcodeCountFilesSampleNames = barcodeCountFilesSampleName;

        // Fixed writers
        if (writeIpcr) {
            plusIpcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix.getPath() + ".plus" + FileExtensions.MACS_IPCR), isZipped, new SampleSumScoreProvider(new String[]{"IPCR"}));
            minusIpcrBedWriter = new BedIpcrRecordWriter(new File(outputPrefix.getPath() + ".minus" + FileExtensions.MACS_IPCR), isZipped, new SampleSumScoreProvider(new String[]{"IPCR"}));
        }

        plusCdnaBedWriters = new HashMap<>();
        minusCdnaBedWriters = new HashMap<>();

        if (scoreProvider != null) {
            // Writers for the plus strand
            BedIpcrRecordWriter cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix.getPath() + ".plus" + FileExtensions.MACS_CDNA), isZipped, scoreProvider);
            plusCdnaBedWriters.put(scoreProvider.getSamplesAsString(), cdnaBedWriter);

            // Writers for the minus strand
            cdnaBedWriter = new BedIpcrRecordWriter(new File(outputPrefix.getPath() + ".minus" + FileExtensions.MACS_CDNA), isZipped, scoreProvider);
            minusCdnaBedWriters.put(scoreProvider.getSamplesAsString(), cdnaBedWriter);

        } else {
            if (barcodeCountFilesSampleNames != null) {
                updateCdnaBedWriters();
            } else {
                LOGGER.info("Initializing writer with no cDNA samples");
            }
        }
    }

    @Override
    public void writeRecord(IpcrRecord record) throws IOException {
        this.writeRecord(record, "");
    }

    @Override
    public void writeRecord(IpcrRecord record, String reason) throws IOException {

        if (record.getOrientation() == '+') {
            if (writeIpcr) {
                plusIpcrBedWriter.writeRecord(record, reason);
            }

            for (BedIpcrRecordWriter cdnaWriter : plusCdnaBedWriters.values()) {
                cdnaWriter.writeRecord(record, reason);
            }
        } else if (record.getOrientation() == '-'){
            if (writeIpcr) {
                minusIpcrBedWriter.writeRecord(record, reason);
            }

            for (BedIpcrRecordWriter cdnaWriter : minusCdnaBedWriters.values()) {
                cdnaWriter.writeRecord(record, reason);
            }
        } else {
            throw new IllegalArgumentException("Ipcr record does not have strand information");
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
            plusIpcrBedWriter.flushAndClose();
            minusIpcrBedWriter.flushAndClose();
        }
        for (BedIpcrRecordWriter cdnaWriter : plusCdnaBedWriters.values()) {
            cdnaWriter.flushAndClose();
        }
        for (BedIpcrRecordWriter cdnaWriter : minusCdnaBedWriters.values()) {
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

                // Plus strand
                if (!plusCdnaBedWriters.containsKey(sample)) {
                    // If it does not exist, add a new writer for the sample
                    BedIpcrRecordWriter writer = new BedIpcrRecordWriter(new File(outputPrefix + "_" + sample + FileExtensions.MACS_CDNA), isZipped,
                            new SampleSumScoreProvider(new String[]{sample}));
                    plusCdnaBedWriters.put(sample, writer);
                } else {
                    // If it does exist, update with new samples
                    plusCdnaBedWriters.get(sample).setBarcodeCountFilesSampleNames(getBarcodeCountFilesSampleNames());
                }

                // Minus strand
                if (!minusCdnaBedWriters.containsKey(sample)) {
                    // If it does not exist, add a new writer for the sample
                    BedIpcrRecordWriter writer = new BedIpcrRecordWriter(new File(outputPrefix + "_" + sample + FileExtensions.MACS_CDNA), isZipped,
                            new SampleSumScoreProvider(new String[]{sample}));
                    minusCdnaBedWriters.put(sample, writer);
                } else {
                    // If it does exist, update with new samples
                    minusCdnaBedWriters.get(sample).setBarcodeCountFilesSampleNames(getBarcodeCountFilesSampleNames());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
