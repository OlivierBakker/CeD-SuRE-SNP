package nl.umcg.suresnp.pipeline.tools.runners;


import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;
import nl.umcg.suresnp.pipeline.barcodes.filters.AdapterSequenceMaxMismatchFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.CsvReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeBarcodeCountsParameters;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeBarcodeCounts {

    private final Logger LOGGER = Logger.getLogger(MakeBarcodeCounts.class);
    private MakeBarcodeCountsParameters params;
    private BufferedWriter discardedWriter;
    private BufferedWriter outputWriter;

    public MakeBarcodeCounts(MakeBarcodeCountsParameters params) throws IOException {
        this.params = params;
        this.discardedWriter = new BufferedWriter(new FileWriter(params.getOutputPrefix() + ".discarded.barcodes.txt"));
        this.outputWriter = new BufferedWriter(new FileWriter(params.getOutputPrefix() + ".barcode.counts"));
    }

    public void run() throws IOException {
        List<InfoRecordFilter> filters = new ArrayList<>();
        filters.add(new AdapterSequenceMaxMismatchFilter(params.getAdapterMaxMismatch()));
        filters.add(new FivePrimeFragmentLengthEqualsFilter(params.getBarcodeLength()));

        // Barcode count map
        Map<String, Integer> barcodeCounts = new HashMap<>();

        // Did not use GenericBarcodeFileReader or SparseBarcodeFileReader for efficiency
        // So the algo can be streaming, otherwise need to refactor to provider type pattern
        for (String curFile : params.getInputBarcodes()) {

            CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(new GenericFile(curFile).getAsInputStream())), "\t");
            List<InfoRecord> barcodeList = new TreeList<>();

            String[] line;
            int curRecord = 0;
            int discarded = 0;
            while ((line = reader.readNext(true)) != null) {
                // Logging
                if (curRecord > 0) {
                    if (curRecord % 1000000 == 0) {
                        LOGGER.info("Processed " + curRecord / 1000000 + " million records");
                    }
                }

                // Initialize filter parameters
                boolean passesFilter = true;
                String reason = null;
                InfoRecord curInfoRecord = null;

                if (line.length == 11) {
                    curInfoRecord = parseBarcodeRecord(line);

                    if (params.isTrimFivePrimeToBarcodeLength()) {
                        int curBcLength = curInfoRecord.getBarcode().length();
                        if (curBcLength > params.getBarcodeLength()) {
                            String trimmedBc = curInfoRecord.getBarcode().substring((curBcLength - params.getBarcodeLength()), curBcLength);
                            curInfoRecord.setBarcode(trimmedBc);
                        }
                    }

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
                    if (barcodeCounts.containsKey(curInfoRecord.getBarcode())) {
                        int newValue = barcodeCounts.get(curInfoRecord.getBarcode()) + 1;
                        barcodeCounts.replace(curInfoRecord.getBarcode(), newValue);
                    } else {
                        barcodeCounts.put(curInfoRecord.getBarcode(), 1);
                    }

                } else {
                    discardedWriter.write(reason + "\t");
                    discardedWriter.write(String.join("\t", line));
                    discardedWriter.newLine();
                    discarded++;
                }
                curRecord++;
            }
            reader.close();
            LOGGER.info("Done, Read " + curRecord + " records");

            if (discarded > 0) {
                LOGGER.warn(discarded + " lines discarded. Discard lines have been written to file: ");
                LOGGER.warn(params.getOutputPrefix() + ".discarded.barcodes.txt");
            }
        }

        discardedWriter.flush();
        discardedWriter.close();


        LOGGER.info(barcodeCounts.size() + " unique barcodes");

        int sanityCheckSum = 0;
        for (String key : barcodeCounts.keySet()) {
            int curValue = barcodeCounts.get(key);
            sanityCheckSum += curValue;

            outputWriter.write(key + "\t" + curValue);
            outputWriter.newLine();
        }
        LOGGER.info("Sanity check " + sanityCheckSum + " total count, should be equal to number below");
        LOGGER.info("Done writing " + barcodeCounts.size() + " records");

        outputWriter.flush();
        outputWriter.close();
    }

    public InfoRecord parseBarcodeRecord(String[] line) throws NumberFormatException {
        InfoRecord curRec = new InfoRecord(line[0].split(" ")[0],
                Integer.parseInt(line[1]),
                Integer.parseInt(line[2]),
                Integer.parseInt(line[3]),
                line[4], null, null, null, null, null);
        return curRec;
    }


}
