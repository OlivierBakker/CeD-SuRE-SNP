package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.InfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.BarebonesInfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.SparseInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeSummariesParameters;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MakeSummaries {


    private static final Logger LOGGER = Logger.getLogger(MakeSummaries.class);
    private static MakeSummariesParameters params;


    public MakeSummaries(MakeSummariesParameters parameters) {
        this.params = parameters;
    }

    public void barcodeOverlap() throws IOException {
        // How many of the cDNA barcodes come back in the iPCR
        InfoFileReader cdnaBarcodeReader = new SparseInfoFileReader(params.getOutputPrefix());

        Set<String> cdnaBarcodes = new HashSet<>(cdnaBarcodeReader.readBarcodeCountFile(new GenericFile(params.getInputBarcodes())).keySet());
        Set<String> ipcrBarcodes = new HashSet<>();

        int inputCdnaCount = cdnaBarcodes.size();

        for (String file : params.getInputIpcr()) {

            Set<String> currentBarcodes;
            switch (params.getInputType()) {
                case "INFO":
                    InfoFileReader ipcrBarcodeReader = new BarebonesInfoFileReader(20);
                    currentBarcodes = ipcrBarcodeReader.getBarcodeSet(new GenericFile(file));
                    break;
                case "IPCR":
                    IpcrRecordProvider ipcrRecordProvider = new IpcrFileReader(new GenericFile(file), true);
                    currentBarcodes = ipcrRecordProvider.getBarcodeSet();
                    break;
                default:
                    throw new IllegalArgumentException("No valid input type provided");
            }

            ipcrBarcodes.addAll(currentBarcodes);
        }

        LOGGER.info("cDNA: " + cdnaBarcodes.size());
        LOGGER.info("iPCR: " + ipcrBarcodes.size());

        cdnaBarcodes.retainAll(ipcrBarcodes);

        LOGGER.info("Overlap: " + cdnaBarcodes.size());

        LOGGER.info(inputCdnaCount +
                " cDNA barcodes  of which " +
                cdnaBarcodes.size() +
                " (" + (cdnaBarcodes.size() / inputCdnaCount) * 100 + "%) could be found in iPCR");

    }


    public void insertSizes() throws IOException {

    }
}
