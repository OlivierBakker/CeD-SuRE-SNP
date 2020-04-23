package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.GenericInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BlockCompressedIpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IterativeMultiFileIpcrReader;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilter;
import nl.umcg.suresnp.pipeline.tools.parameters.RecodeParameters;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class Recode {

    private static final Logger LOGGER = Logger.getLogger(Recode.class);
    private RecodeParameters params;

    public Recode(RecodeParameters params) {
        this.params = params;
    }

    public void run() throws IOException, ParseException {

        LOGGER.warn("All input iPCR files must have the same cDNA samples available, otherwise unexpected behavior might occur.");
        // Read additional cDNA data
        Map<String, Map<String, Integer>> inputCdna = readCdnaCounts();

        // Filters to apply
        List<IpcrRecordFilter> filters;
        if (params.getFilters() != null) {
            filters = params.getFilters();
        } else {
            filters = new ArrayList<>();
        }

        // Define input reader, needs to be done here as to know which samples are available.
        // If a indexed iPCR file is provided, init a BlockCompressedIpcrReader
        IpcrRecordProvider ipcrReader;
        if (params.getInputIpcr().length > 1 || !params.getInputType().equals("IPCR_INDEXED")) {
            LOGGER.info("Using generic IpcrFileReader. Using multiple ipcr files with Tabix is currently not supported");
            ipcrReader = new IterativeMultiFileIpcrReader(params.getInputIpcr());
            // When not using tabix indexed file reader apply classical filtering
            if (params.getRegionFilterFile() != null) {
                LOGGER.info("Will loop over all records to extract loci");
                filters.add(new InRegionFilter(params.getRegionFilterFile()));
            }
        } else {
            LOGGER.info("Using BlockCompressedIpcrFileReader");
            if (params.getRegionFilterFile() != null) {
                LOGGER.info("Using Tabix for fast extraction of loci");
                ipcrReader = new BlockCompressedIpcrFileReader(new GenericFile(params.getInputIpcr()[0]), new InRegionFilter(params.getRegionFilterFile()));
            } else {
                ipcrReader = new BlockCompressedIpcrFileReader(new GenericFile(params.getInputIpcr()[0]));
            }
        }

        // Define the output writer
        IpcrOutputWriter writer = params.getOutputWriter();
        if (params.isReplaceOldCdnaSamples()) {
            // Keep only the newly added samples
            writer.setBarcodeCountFilesSampleNames(writer.getBarcodeCountFilesSampleNames());
        } else {
            // Concat the sample names form the existing file and the new ones
            writer.setBarcodeCountFilesSampleNames(ArrayUtils.addAll(writer.getBarcodeCountFilesSampleNames(), ipcrReader.getCdnaSamples()));
        }
        writer.writeHeader();


        long start = System.currentTimeMillis();
        int totalRecords = 0;
        int filteredRecords = 0;
        int writtenRecords = 0;

        for (IpcrRecord rec : ipcrReader) {
            totalRecords ++;
            logProgress(totalRecords, 1000000, "Recode");

            // Apply filters
            boolean passesFilter = true;
            for (IpcrRecordFilter filter : filters) {
                if (!filter.passesFilter(rec)) {
                    passesFilter=false;
                    filteredRecords ++;
                    break;
                }
            }

            if (passesFilter) {
                // If provided, add additional cDNA data to ipcr records
                if (inputCdna != null) {
                    for (String curCdnaFile : inputCdna.keySet()) {
                        Map<String, Integer> curMap = inputCdna.get(curCdnaFile);
                        Integer curCdnaCount = curMap.get(rec.getBarcode());

                        if (curCdnaCount != null) {
                            rec.addBarcodeCount(curCdnaFile, curCdnaCount);
                        } else {
                            rec.addBarcodeCount(curCdnaFile, 0);
                        }
                    }
                }

                // Write output
                writer.writeRecord(rec);
                writtenRecords++;
            }
        }
        writer.flushAndClose();
        long stop = System.currentTimeMillis();

        LOGGER.info("Done writing. Took: " + ((stop - start) / 1000) + " seconds");
        LOGGER.info("Processed  " + totalRecords + " records");
        LOGGER.info("Wrote  " + writtenRecords + " records");
        LOGGER.info("Removed  " + filteredRecords + " records");
    }

    private Map<String, Map<String, Integer>> readCdnaCounts() throws IOException {

        Map<String, Map<String, Integer>> inputCdna;
        if (params.getInputCdna() != null) {
            long start = System.currentTimeMillis();
            inputCdna = new HashMap<>();

            for (String file : params.getInputCdna()) {
                GenericFile inputFile = new GenericFile(file);
                LOGGER.info("Reading sample: " + inputFile.getBaseName());
                inputCdna.put(inputFile.getBaseName(), GenericInfoFileReader.readBarcodeCountFile(inputFile));
            }

            long stop = System.currentTimeMillis();
            LOGGER.info("Done reading. Took: " + ((stop - start) / 1000) + " seconds");

            LOGGER.info("Adding the following samples to the iPCR records");
            StringBuilder logLine = new StringBuilder();
            for (String file: inputCdna.keySet()) {
                logLine.append(file).append(", ");
            }
            LOGGER.info(logLine.toString());

        } else {
            inputCdna = null;
        }
        return inputCdna;
    }
}
