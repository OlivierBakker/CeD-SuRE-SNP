package nl.umcg.suresnp.pipeline.io.barcodefilereader;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.CsvReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericBarcodeFileReader implements BarcodeFileReader {
    private static final Logger LOGGER = Logger.getLogger(GenericBarcodeFileReader.class);
    private String outputPrefix;
    private BufferedWriter writer;

    // TODO: cleanup duplicated code
    public GenericBarcodeFileReader(String outputPrefix) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(outputPrefix + ".discarded.barcodes.txt"));
        this.outputPrefix = outputPrefix;
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFile(GenericFile file) throws IOException {
        return readBarcodeFile(file, new ArrayList<>());
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFile(GenericFile file, List<InfoRecordFilter> filters) throws IOException {

        // May seem excessive, but allows for easy change to zipped files if needed
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(file.getAsInputStream())), "\t");

        Map<String, InfoRecord> barcodeRecordMap = new HashMap<>();

        String[] line;
        int curRecord = 0;
        int discarded = 0;
        while ((line = reader.readNext(true)) != null) {
            // Logging
            if (curRecord > 0) {
                if (curRecord % 1000000 == 0) {
                    LOGGER.info("Read " + curRecord / 1000000 + " million records");
                }
            }

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
                barcodeRecordMap.put(curInfoRecord.getReadId(), curInfoRecord);
            } else {
                writer.write(reason + "\t");
                writer.write(String.join("\t", line));
                writer.newLine();
                discarded++;
            }

            curRecord++;

        }
        reader.close();
        LOGGER.info("Done, Read " + curRecord + " records");

        if (discarded > 0) {
            LOGGER.warn(discarded + " lines discarded. Discard lines have been written to file: ");
            LOGGER.warn(outputPrefix + "/" + file.getBaseName() + ".discarded.barcodes.txt");
        }

        return barcodeRecordMap;
    }

    @Override
    public Map<String, String> readBarcodeFileAsStringMap(GenericFile file) throws IOException {
        return readBarcodeFileAsStringMap(file, new ArrayList<>());
    }

    @Override
    public Map<String, String> readBarcodeFileAsStringMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        // May seem excessive, but allows for easy change to zipped files if needed
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(file.getAsInputStream())), "\t");

        Map<String, String> barcodeRecordMap = new HashMap<>();

        String[] line;
        int curRecord = 0;
        int discarded = 0;

        while ((line = reader.readNext(true)) != null) {
            // Logging
            if (curRecord > 0) {
                if (curRecord % 1000000 == 0) {
                    LOGGER.info("Read " + curRecord / 1000000 + " million records");
                }
            }

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
                barcodeRecordMap.put(curInfoRecord.getReadId(), curInfoRecord.getBarcode());
            } else {
                writer.write(reason + "\t");
                writer.write(String.join("\t", line));
                writer.newLine();
                discarded++;
            }

            curRecord++;

        }
        reader.close();
        LOGGER.info("Done, Read " + curRecord + " records");

        if (discarded > 0) {
            LOGGER.warn(discarded + " lines discarded. Discard lines have been written to file: ");
            LOGGER.warn(outputPrefix + "/" + file.getBaseName() + ".discarded.barcodes.txt");
        }

        return barcodeRecordMap;    }


    @Override
    public void close() throws IOException {
        writer.close();
    }

    private static InfoRecord parseBarcodeRecord(String[] line) throws NumberFormatException {
        InfoRecord curRec = new InfoRecord(line[0].split(" ")[0],
                Integer.parseInt(line[1]),
                Integer.parseInt(line[2]),
                Integer.parseInt(line[3]),
                line[4], line[8], line[5], line[9], line[6], line[10]);
        return curRec;

    }

    @Override
    public Map<String, Integer> readBarcodeCountFile(File inputBarcodes) throws IOException {
        // Open a new CSV reader
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(new FileInputStream(inputBarcodes))), "\t");
        Map<String, Integer> readBarcodePairs = new HashMap<>();
        String[] line;
        int i = 0;

        while ((line = reader.readNext(false)) != null) {
            // Logging progress
            if (i > 0){if(i % 1000000 == 0){LOGGER.info("Read " + i / 1000000 + " million records");}}
            i++;

            if (line.length != 2) {
                continue;
            } else {
                readBarcodePairs.put(line[0], Integer.parseInt(line[1]));
            }
        }
        reader.close();

        return readBarcodePairs;
    }
}
