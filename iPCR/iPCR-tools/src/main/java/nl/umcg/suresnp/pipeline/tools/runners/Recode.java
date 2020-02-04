package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.GenericInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BinaryIpcrReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.RecodeParameters;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
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

    public void run() throws IOException {

        LOGGER.warn("All input iPCR files must have the same cDNA sample availible, otherwise unexpected behavior might occur.");
        // TODO: Implement filters (already done for reader class so its just adding some commandline arguments)
        List<IpcrRecord> inputIpcr = readIpcrRecords();
        Map<String, Map<String, Integer>> inputCdna = readCdnaCounts();

        // Writing output. Concat the sample names form the existing file and the new ones
        IpcrOutputWriter writer = params.getOutputWriter();
        writer.setBarcodeCountFilesSampleNames(ArrayUtils.addAll(writer.getBarcodeCountFilesSampleNames(), provider.getCdnaSamples()));
        writer.writeHeader();

        long start = System.currentTimeMillis();
        for (IpcrRecord rec : inputIpcr) {
            for (String curCdnaFile : inputCdna.keySet()) {
                Map<String, Integer> curMap = inputCdna.get(curCdnaFile);
                Integer curCdnaCount = curMap.get(rec.getBarcode());

                if (curCdnaCount != null) {
                    rec.addBarcodeCount(curCdnaFile, curCdnaCount);
                } else {
                    rec.addBarcodeCount(curCdnaFile, 0);
                }
            }
            writer.writeRecord(rec);
        }
        long stop = System.currentTimeMillis();
        LOGGER.info("Done writing. Took: " + ((stop - start) / 1000) + " seconds");
        writer.flushAndClose();

    }


    private Map<String, Map<String, Integer>> readCdnaCounts() throws IOException {
        long start = System.currentTimeMillis();
        Map<String, Map<String, Integer>> inputCdna = new HashMap<>();

        for (String file : params.getInputCdna()) {
            GenericFile inputFile = new GenericFile(file);
            LOGGER.info("Reading file: " + inputFile.getBaseName());
            inputCdna.put(inputFile.getBaseName(), GenericInfoFileReader.readBarcodeCountFile(inputFile));
        }

        long stop = System.currentTimeMillis();
        LOGGER.info("Done reading. Took: " + ((stop - start) / 1000) + " seconds");

        return inputCdna;
    }

    private List<IpcrRecord> readIpcrRecords() throws IOException {
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

            inputIpcr.addAll(provider.getRecordsAsList());
            provider.close();
        }

        long stop = System.currentTimeMillis();
        LOGGER.info("Done reading. Took: " + ((stop - start) / 1000) + " seconds");

        return inputIpcr;
    }
}
