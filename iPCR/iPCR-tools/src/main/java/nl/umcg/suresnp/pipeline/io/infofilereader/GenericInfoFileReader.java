package nl.umcg.suresnp.pipeline.io.infofilereader;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.CsvReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;


import java.io.*;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class GenericInfoFileReader implements InfoFileReader {
    private static final Logger LOGGER = Logger.getLogger(GenericInfoFileReader.class);
    private String outputPrefix;
    private BufferedWriter writer;

    // TODO: cleanup duplicated code
    public GenericInfoFileReader(String outputPrefix) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(outputPrefix + ".discarded.barcodes.txt"));
        this.outputPrefix = outputPrefix;
    }

    @Override
    public List<InfoRecord> readBarcodeFileAsList(GenericFile file) throws IOException {
        return readBarcodeFileAsList(file, new ArrayList<>());
    }

    @Override
    public List<InfoRecord> readBarcodeFileAsList(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(file.getAsInputStream())), "\t");
        List<InfoRecord> barcodeList = new TreeList<>();

        String[] line;
        int curRecord = 0;
        int discarded = 0;
        while ((line = reader.readNext(true)) != null) {
            logProgress(curRecord, 1000000, "GenericBarcodeFileReader");

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
                barcodeList.add(curInfoRecord);
            } else {
                writer.write(reason + "\t");
                writer.write(String.join("\t", line));
                writer.newLine();
                discarded++;
            }

            curRecord++;

        }
        System.out.print("\n"); // Flush progress bar
        reader.close();
        LOGGER.info("Done, Read " + curRecord + " records");

        if (discarded > 0) {
            LOGGER.warn(discarded + " lines discarded. Discard lines have been written to file: ");
            LOGGER.warn(outputPrefix + "/" + file.getBaseName() + ".discarded.barcodes.txt");
        }

        return barcodeList;
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file) throws IOException {
        return readBarcodeFileAsMap(file, new ArrayList<>());
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException {

        // May seem excessive, but allows for easy change to zipped files if needed
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(file.getAsInputStream())), "\t");
        Map<String, InfoRecord> barcodeRecordMap = new HashMap<>();

        String[] line;
        int curRecord = 0;
        int discarded = 0;
        while ((line = reader.readNext(true)) != null) {
            logProgress(curRecord, 1000000, "GenericBarcodeFileReader");

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
        System.out.print("\n"); // Flush progress bar
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
        LOGGER.info("Reading file: " + file.getBaseName());

        while ((line = reader.readNext(true)) != null) {
            logProgress(curRecord, 1000000, "GenericBarcodeFileReader");

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
        System.out.print("\n"); // Flush progress bar
        reader.close();
        LOGGER.info("Done, Read " + curRecord + " records");

        if (discarded > 0) {
            LOGGER.warn(discarded + " lines discarded. Discard lines have been written to file: ");
            LOGGER.warn(outputPrefix + "/" + file.getBaseName() + ".discarded.barcodes.txt");
        }
        return barcodeRecordMap;
    }

    @Override
    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
    }

    public InfoRecord parseBarcodeRecord(String[] line) throws NumberFormatException {
        InfoRecord curRec = new InfoRecord(line[0].split(" ")[0],
                Integer.parseInt(line[1]),
                Integer.parseInt(line[2]),
                Integer.parseInt(line[3]),
                line[4], line[8], line[5], line[9], line[6], line[10]);
        return curRec;

    }

    @Override
    public Map<String, Integer> readBarcodeCountFile(GenericFile inputBarcodes) throws IOException {
        // Open a new CSV reader
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(inputBarcodes.getAsInputStream())), "\t");
        Map<String, Integer> readBarcodePairs = new HashMap<>();
        String[] line;
        int i = 0;

        LOGGER.info("Reading file: " + inputBarcodes.getBaseName());

        while ((line = reader.readNext(false)) != null) {
            logProgress(i, 1000000, "GenericBarcodeFileReader");
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

    @Override
    public Set<String> getBarcodeSet(GenericFile file) throws IOException {
        throw new UnsupportedOperationException();
    }
}
