package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.GenericInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.*;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilterType;
import nl.umcg.suresnp.pipeline.records.summarystatistic.filters.FilterType;
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

        // Filter for genomic regions. Defined seperately for speedy tabix reading
        InRegionFilter regionFilter = null;
        if (params.getRegionFilterFile() != null) {
            LOGGER.info("Applying region filter");
            regionFilter = new InRegionFilter(params.getRegionFilterFile(), false);
        }

        // Define input reader, needs to be done here as to know which samples are available.
        // If a indexed iPCR file is provided, init a BlockCompressedIpcrReader
        IpcrRecordProvider ipcrReader;

        if (!params.getInputType().equals("IPCR_INDEXED")) {
            // Non-indexed iPCR files
            if (params.getInputIpcr().length > 1) {
                LOGGER.info("Using IterativeMultiFileIpcrReader");
                ipcrReader = new IterativeMultiFileIpcrReader(params.getInputIpcr());
            } else {
                LOGGER.info("Using generic IpcrReader");
                ipcrReader = new IpcrFileReader(new GenericFile(params.getInputIpcr()[0]), true);
            }

            // When not using tabix indexed file reader apply classical filtering
            if (regionFilter != null) {
                LOGGER.info("Will loop over all records to extract loci");
                filters.add(regionFilter);
            }
        } else {
            // Indexed iPCR files
            if (params.getInputIpcr().length > 1) {
                LOGGER.info("Using MultiFileBlockCompressedIpcrFileReader");
                if (regionFilter != null) {
                    ipcrReader = new MultiFileBlockCompressedIpcrFileReader(params.getInputIpcr(), regionFilter);
                } else {
                    ipcrReader = new MultiFileBlockCompressedIpcrFileReader(params.getInputIpcr());
                }
            } else {
                LOGGER.info("Using BlockCompressedIpcrFileReader");
                if (regionFilter != null) {
                    ipcrReader = new BlockCompressedIpcrFileReader(new GenericFile(params.getInputIpcr()[0]), regionFilter);
                } else {
                    ipcrReader = new BlockCompressedIpcrFileReader(new GenericFile(params.getInputIpcr()[0]));
                }
            }
        }

        LOGGER.info("Applying the following filters:");
        for (IpcrRecordFilter filter : filters) {
            LOGGER.info(filter.getFilterName());
        }

        // Define the output writer
        IpcrOutputWriter writer = params.getOutputWriter();
        if (params.isReplaceOldCdnaSamples()) {
            if (params.getSamplesToWrite() == null) {
                // Keep only the newly added samples
                writer.setBarcodeCountFilesSampleNames(writer.getBarcodeCountFilesSampleNames());
            }
        } else {
            if (params.getSamplesToWrite() == null) {
                // Concat the sample names form the existing file and the new ones
                writer.setBarcodeCountFilesSampleNames(ArrayUtils.addAll(writer.getBarcodeCountFilesSampleNames(), ipcrReader.getCdnaSamples()));
            }
        }
        writer.writeHeader();


        long start = System.currentTimeMillis();
        int totalRecords = 0;
        int filteredRecords = 0;
        int writtenRecords = 0;

        for (IpcrRecord rec : ipcrReader) {
            totalRecords++;
            logProgress(totalRecords, 1000000, "Recode");

            // Apply filters
            boolean passesFilter = true;
            for (IpcrRecordFilter filter : filters) {
                if (!filter.passesFilter(rec)) {
                    passesFilter = false;
                    filteredRecords++;
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
            for (String file : inputCdna.keySet()) {
                logLine.append(file).append(", ");
            }
            LOGGER.info(logLine.toString());

        } else {
            inputCdna = null;
        }
        return inputCdna;
    }
}
