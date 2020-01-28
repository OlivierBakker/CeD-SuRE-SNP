package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.InfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.BarebonesInfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.SparseInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeSummariesParameters;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

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


    public void getInsertSizes() throws IOException, IllegalArgumentException {

        long insertSizeTotal = 0;
        long totalIpcrCount = 0;

        for (String file : params.getInputIpcr()) {
            switch (params.getInputType()) {

                case "IPCR":
                    IpcrRecordProvider ipcrRecordProvider = new IpcrFileReader(new GenericFile(file), true);
                    IpcrRecord curRecord = ipcrRecordProvider.getNextRecord();
                    long curRecordCount = 0;
                    long curInsertSize = 0;
                    long curInsertSize2 = 0;

                    while (curRecord != null) {
                        logProgress(curRecordCount, 1000000, "MakeSummaries");

                        insertSizeTotal = insertSizeTotal + (curRecord.getOrientationIndependentEnd() - curRecord.getOrientationIndependentStart());
                        curInsertSize = curInsertSize + (curRecord.getOrientationIndependentEnd() - curRecord.getOrientationIndependentStart());

                        curRecordCount ++;
                        totalIpcrCount ++;

                        curRecord = ipcrRecordProvider.getNextRecord();
                    }


                    LOGGER.info("CurInsertSize: " + curInsertSize);
                    LOGGER.info("CurInserSize2: " + curInsertSize2);
                    LOGGER.info("CurRecordCount: " + curRecordCount);
                    LOGGER.info(file + "\t" + Math.round((double)curInsertSize / (double)curRecordCount));
                    LOGGER.info(file + "\t" + Math.round((double) curInsertSize2 / (double) curRecordCount));

                    break;
                default:
                    throw new IllegalArgumentException("Insert Sizes only supports IPCR input");
            }
        }

        LOGGER.info("Total insert size: "  + insertSizeTotal);
        LOGGER.info("Total ipcr count size: "  + totalIpcrCount);

        LOGGER.info("Average insert size: " + Math.round((double) insertSizeTotal / (double) totalIpcrCount));
    }
}
