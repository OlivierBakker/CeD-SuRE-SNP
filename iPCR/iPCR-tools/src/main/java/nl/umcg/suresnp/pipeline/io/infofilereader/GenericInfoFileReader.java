package nl.umcg.suresnp.pipeline.io.infofilereader;

import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;
import nl.umcg.suresnp.pipeline.records.inforecord.consumers.*;
import nl.umcg.suresnp.pipeline.records.inforecord.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.CsvReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class GenericInfoFileReader implements InfoFileReader {
    private static final Logger LOGGER = Logger.getLogger(GenericInfoFileReader.class);
    private String outputPrefix;
    private BufferedWriter writer;
    private boolean writeDiscardedOutput;

    public GenericInfoFileReader(String outputPrefix) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(outputPrefix + ".discarded.barcodes.txt"));
        this.outputPrefix = outputPrefix;
        this.writeDiscardedOutput = true;
    }

    public GenericInfoFileReader(String outputPrefix, boolean writeDiscardedOutput) throws IOException {
        this.outputPrefix = outputPrefix;
        this.writeDiscardedOutput = writeDiscardedOutput;
        if (this.writeDiscardedOutput) {
            this.writer = new BufferedWriter(new FileWriter(outputPrefix + ".discarded.barcodes.txt"));
        } else {
            this.writer = null;
        }
    }

    @Override
    public List<InfoRecord> readBarcodeFileAsList(GenericFile file) throws IOException {
        return readBarcodeFileAsList(file, new ArrayList<>());
    }

    @Override
    public List<InfoRecord> readBarcodeFileAsList(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        ListInfoRecordConsumer consumer = new ListInfoRecordConsumer();
        readInfoFile(file, filters, consumer);
        return consumer.getOutput();
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file) throws IOException {
        return readBarcodeFileAsMap(file, new ArrayList<>());
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        MapInfoRecordConsumer consumer = new MapInfoRecordConsumer();
        readInfoFile(file, filters, consumer);
        return consumer.getOutput();
    }

    @Override
    public Map<String, String> readBarcodeFileAsStringMap(GenericFile file) throws IOException {
        return readBarcodeFileAsStringMap(file, new ArrayList<>());
    }

    @Override
    public Map<String, String> readBarcodeFileAsStringMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        StringMapInfoRecordConsumer consumer = new StringMapInfoRecordConsumer();
        readInfoFile(file, filters, consumer);
        return consumer.getOutput();
    }

    @Override
    public Set<String> getBarcodeSet(GenericFile file) throws IOException {
        BarcodeSetInfoRecordConsumer consumer = new BarcodeSetInfoRecordConsumer();
        readInfoFile(file, new ArrayList<>(), consumer);
        return consumer.getOutput();
    }

    @Override
    public Set<String> getBarcodeSet(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        BarcodeSetInfoRecordConsumer consumer = new BarcodeSetInfoRecordConsumer();
        readInfoFile(file, filters, consumer);
        return consumer.getOutput();
    }

    @Override
    public List<String> getBarcodeList(GenericFile file) throws IOException {
        BarcodeListInfoRecordConsumer consumer = new BarcodeListInfoRecordConsumer();
        readInfoFile(file, new ArrayList<>(), consumer);
        return consumer.getOutput();
    }

    @Override
    public List<String> getBarcodeList(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        BarcodeListInfoRecordConsumer consumer = new BarcodeListInfoRecordConsumer();
        readInfoFile(file, filters, consumer);
        return consumer.getOutput();
    }

    @Override
    public void flushAndClose() throws IOException {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    public static Map<String, Integer> readBarcodeCountFile(GenericFile inputBarcodes) throws IOException {
        // Open a new CSV reader
        CsvReader reader = new CsvReader(inputBarcodes.getAsBufferedReader(), "\t");
        Map<String, Integer> readBarcodePairs = new HashMap<>();
        String[] line;
        int i = 0;

        LOGGER.info("Reading file: " + inputBarcodes.getFileName());

        while ((line = reader.readNext(false)) != null) {
            logProgress(i, 1000000, "GenericInfoFileReader");
            i++;

            if (line.length != 2) {
                continue;
            } else {
                readBarcodePairs.put(line[0], Integer.parseInt(line[1]));
            }
        }
        System.out.print("\n"); // Flush progress bar
        reader.close();

        return readBarcodePairs;
    }

    public InfoRecord parseBarcodeRecord(String[] line) throws NumberFormatException {
        InfoRecord curRec = new InfoRecord(line[0].split(" ")[0],
                Integer.parseInt(line[1]),
                Integer.parseInt(line[2]),
                Integer.parseInt(line[3]),
                line[4], line[8], line[5], line[9], line[6], line[10]);
        return curRec;

    }

    private void readInfoFile(GenericFile file, List<InfoRecordFilter> filters, InfoRecordConsumer outputConsumer) throws IOException {
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(file.getAsInputStream())), "\t");
        String[] line;
        int curRecord = 0;
        int discarded = 0;

        while ((line = reader.readNext(true)) != null) {
            logProgress(curRecord, 1000000, "GenericInfoFileReader");

            // Initialize filter parameters
            boolean passesFilter = true;
            String reason = null;
            InfoRecord curInfoRecord = null;

            if (line.length == 11) {
                curInfoRecord = parseBarcodeRecord(line);
                for (InfoRecordFilter filter : filters) {
                    if (!filter.passesFilter(curInfoRecord)) {
                        passesFilter = false;
                        reason = filter.getFilterName();
                        break;
                    }
                }
            } else {
                reason = "ColumnCountFilter";
                passesFilter = false;
            }

            if (passesFilter) {
                outputConsumer.proccesInfoRecord(curInfoRecord);
            } else {
                if (writeDiscardedOutput) {
                    writer.write(reason + "\t");
                    writer.write(String.join("\t", line));
                    writer.newLine();
                }
                discarded++;
            }
            curRecord++;
        }

        System.out.print("\n"); // Flush progress bar
        reader.close();
        LOGGER.info("Done, Read " + curRecord + " records");
        LOGGER.warn(discarded + " lines discarded");

        if (discarded > 0 && writeDiscardedOutput) {
            LOGGER.warn("Discard lines have been written to file: ");
            LOGGER.warn(outputPrefix + "/" + file.getBaseName() + ".discarded.barcodes.txt");
            writer.flush();
            writer.close();
        }

    }
}
