package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.GenericInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BinaryIpcrReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.ipcrrecords.filters.IpcrRecordFilter;
import nl.umcg.suresnp.pipeline.tools.parameters.RecodeParameters;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recode {

    private static final Logger LOGGER = Logger.getLogger(Recode.class);
    private RecodeParameters params;
    private IpcrRecordProvider provider;

    public Recode(RecodeParameters params) {
        this.params = params;
    }

    public void run() throws IOException, ParseException {

        LOGGER.warn("All input iPCR files must have the same cDNA samples available, otherwise unexpected behavior might occur.");
        List<IpcrRecord> inputIpcr = readIpcrRecords();

        // Writing output. Concat the sample names form the existing file and the new ones
        IpcrOutputWriter writer = params.getOutputWriter();
        writer.setBarcodeCountFilesSampleNames(ArrayUtils.addAll(writer.getBarcodeCountFilesSampleNames(), provider.getCdnaSamples()));
        writer.writeHeader();

        // Read additional cDNA data
        Map<String, Map<String, Integer>> inputCdna = readCdnaCounts();

        long start = System.currentTimeMillis();
        for (IpcrRecord rec : inputIpcr) {

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
        }

        writer.flushAndClose();
        long stop = System.currentTimeMillis();
        LOGGER.info("Done writing. Took: " + ((stop - start) / 1000) + " seconds");
    }

    private Map<String, Map<String, Integer>> readCdnaCounts() throws IOException {
        Map<String, Map<String, Integer>> inputCdna;
        if (params.getInputCdna() != null) {
            long start = System.currentTimeMillis();
            inputCdna = new HashMap<>();

            for (String file : params.getInputCdna()) {
                GenericFile inputFile = new GenericFile(file);
                LOGGER.info("Reading file: " + inputFile.getBaseName());
                inputCdna.put(inputFile.getBaseName(), GenericInfoFileReader.readBarcodeCountFile(inputFile));
            }

            long stop = System.currentTimeMillis();
            LOGGER.info("Done reading. Took: " + ((stop - start) / 1000) + " seconds");
        } else {
            inputCdna = null;
        }
        return inputCdna;
    }

    private List<IpcrRecord> readIpcrRecords() throws IOException, ParseException {

        // Region filter
        List<IpcrRecordFilter> filters = new ArrayList<>();
        if (params.getRegionFilterFile() != null) {
            filters.add(new InRegionFilter(params.getRegionFilterFile()));
        }

        long start = System.currentTimeMillis();
        List<IpcrRecord> inputIpcr = new ArrayList<>();

        for (String file : params.getInputIpcr()) {
            GenericFile inputFile = new GenericFile(file);
            LOGGER.info("Reading file: " + inputFile.getBaseName());

            switch (params.getInputType()) {
                case "IPCR":
                    provider = new IpcrFileReader(inputFile, true);
                    break;
                case "IPCR_BIN":
                    provider = new BinaryIpcrReader(inputFile);
                    break;
                default:
                    throw new IllegalArgumentException("No valid input type provided");
            }

            if (filters.size() > 0) {
                inputIpcr.addAll(provider.getRecordsAsList(filters));
            } else {
                inputIpcr.addAll(provider.getRecordsAsList());
            }
            provider.close();
        }

        long stop = System.currentTimeMillis();
        LOGGER.info("Done reading. Took: " + ((stop - start) / 1000) + " seconds");

        return inputIpcr;
    }
}
