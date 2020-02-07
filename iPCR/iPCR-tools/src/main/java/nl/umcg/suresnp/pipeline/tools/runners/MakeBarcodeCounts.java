package nl.umcg.suresnp.pipeline.tools.runners;


import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;
import nl.umcg.suresnp.pipeline.records.inforecord.filters.AdapterSequenceMaxMismatchFilter;
import nl.umcg.suresnp.pipeline.records.inforecord.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.records.inforecord.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.CsvReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeBarcodeCountsParameters;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class MakeBarcodeCounts {

    private final Logger LOGGER = Logger.getLogger(MakeBarcodeCounts.class);
    private MakeBarcodeCountsParameters params;
    private BufferedWriter discardedWriter;
    private BufferedWriter outputWriter;
    private BufferedWriter barcodeWriter;

    public MakeBarcodeCounts(MakeBarcodeCountsParameters params) throws IOException {
        this.params = params;

        String suffix = ""; if (params.isZipped()) suffix=".gz";
        this.discardedWriter = new GenericFile(params.getOutputPrefix() + ".discarded.barcodes.txt" + suffix).getAsBufferedWriter();
        this.outputWriter = new GenericFile(params.getOutputPrefix() + ".barcode.counts" + suffix).getAsBufferedWriter();
        this.barcodeWriter = new GenericFile(params.getOutputPrefix() + ".barcodes" + suffix).getAsBufferedWriter();
    }

    public void run() throws IOException {

        List<InfoRecordFilter> filters = new ArrayList<>();
        filters.add(new AdapterSequenceMaxMismatchFilter(params.getAdapterMaxMismatch()));
        filters.add(new FivePrimeFragmentLengthEqualsFilter(params.getBarcodeLength()));

        // Barcode count map
        Map<String, Integer> barcodeCounts = new HashMap<>();

        // Logging variables
        int totalCount=0;
        int totalDiscard=0;

        // Did not use GenericBarcodeFileReader or SparseBarcodeFileReader for efficiency
        // So the algo can be streaming, otherwise need to refactor to provider type pattern
        for (String curFile : params.getInputBarcodes()) {

            CsvReader reader = new CsvReader(new GenericFile(curFile).getAsBufferedReader(), "\t");

            String[] line;
            int curRecord = 0;
            int discarded = 0;
            while ((line = reader.readNext(true)) != null) {
                logProgress(curRecord, 1000000, "MakeBarcodeCounts");

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

                    if (params.isWriteBarcodeFile()) {
                        barcodeWriter.write(curInfoRecord.getBarcode());
                        barcodeWriter.newLine();
                    }

                } else {
                    discardedWriter.write(reason + "\t");
                    discardedWriter.write(String.join("\t", line));
                    discardedWriter.newLine();
                    discarded++;
                }
                curRecord++;
            }
            System.out.print("\n"); // Flush progress bar

            reader.close();
            LOGGER.info("Done, Read " + curRecord + " records");
            totalCount += curRecord;

            if (discarded > 0) {
                totalDiscard += discarded;
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
        LOGGER.info("Sanity check " + sanityCheckSum + " total count, should be equal to " + (totalCount - totalDiscard));
        LOGGER.info("Done writing " + barcodeCounts.size() + " records");

        outputWriter.flush();
        outputWriter.close();

        barcodeWriter.flush();
        barcodeWriter.close();

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
