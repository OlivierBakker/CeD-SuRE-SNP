package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.inforecords.filters.BarcodeContainedInFilter;
import nl.umcg.suresnp.pipeline.inforecords.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.inforecords.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.InfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.SparseInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeSummariesParameters;
import nl.umcg.suresnp.pipeline.utils.StreamingHistogram;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

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
        Set<String> ipcrBarcodes = readIpcrBarcodesAsSet();

        int inputCdnaCount = cdnaBarcodes.size();

        LOGGER.info("Trimming barcode sets");
        cdnaBarcodes = (Set<String>) trimBarcodesFivePrime(cdnaBarcodes, new HashSet<>(), 2);
        ipcrBarcodes = (Set<String>) trimBarcodesFivePrime(ipcrBarcodes, new HashSet<>(), 2);

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
        StreamingHistogram hist = new StreamingHistogram(10, 20);

        for (String file : params.getInputIpcr()) {
            switch (params.getInputType()) {
                case "IPCR":
                    IpcrRecordProvider ipcrRecordProvider = new IpcrFileReader(new GenericFile(file), true);
                    IpcrRecord curRecord = ipcrRecordProvider.getNextRecord();
                    long curRecordCount = 0;
                    long curInsertSizeTotal = 0;

                    while (curRecord != null) {
                        logProgress(curRecordCount, 1000000, "MakeSummaries");

                        int curInsertSize = curRecord.getOrientationIndependentEnd() - curRecord.getOrientationIndependentStart();
                        hist.addPostiveValue(curInsertSize);

                        insertSizeTotal = insertSizeTotal + curInsertSize;
                        curInsertSizeTotal = curInsertSizeTotal + curInsertSize ;

                        curRecordCount ++;
                        totalIpcrCount ++;

                        curRecord = ipcrRecordProvider.getNextRecord();
                    }
                    System.out.print("\n"); // Flush progress bar

                    LOGGER.info("CurInsertSize: " + curInsertSizeTotal);
                    LOGGER.info("CurRecordCount: " + curRecordCount);
                    LOGGER.info(file + "\t" + Math.round((double)curInsertSizeTotal / (double)curRecordCount));

                    break;
                default:
                    throw new IllegalArgumentException("Insert Sizes only supports IPCR input");
            }
        }

        LOGGER.info("Total insert size: "  + insertSizeTotal);
        LOGGER.info("Total ipcr count size: "  + totalIpcrCount);
        LOGGER.info("Average insert size: " + Math.round((double) insertSizeTotal / (double) totalIpcrCount));
        LOGGER.info("Histrogram:");

        System.out.print(hist.getHistAsString());

    }

    public void barcodeOverlapWriteOut() throws IOException {

        // How many of the cDNA barcodes come back in the iPCR
        Set<String> ipcrBarcodes = readIpcrBarcodesAsSet();

        // Write overlapping barcodes
        List<InfoRecordFilter> filters =  new ArrayList<>();
        filters.add(new FivePrimeFragmentLengthEqualsFilter(20));

        InfoFileReader cdnaBarcodeReader = new SparseInfoFileReader(params.getOutputPrefix(), false);
        List<String> cdnaBarcodes = cdnaBarcodeReader.getBarcodeList(new GenericFile(params.getInputBarcodes()), filters);
        cdnaBarcodeReader.flushAndClose();

        BufferedWriter overlappingOutputWriter = new BufferedWriter(new OutputStreamWriter( new GenericFile(params.getOutputPrefix() + ".overlapping.barcodes").getAsOutputStream()));
        BufferedWriter nonOverlappingOutputWriter = new BufferedWriter(new OutputStreamWriter( new GenericFile(params.getOutputPrefix() + ".non.overlapping.barcodes").getAsOutputStream()));

        for (String curBarcode: cdnaBarcodes) {
            if (ipcrBarcodes.contains(curBarcode)) {
                overlappingOutputWriter.write(curBarcode);
                overlappingOutputWriter.newLine();
            } else {
                nonOverlappingOutputWriter.write(curBarcode);
                nonOverlappingOutputWriter.newLine();
            }
        }

        overlappingOutputWriter.flush();
        overlappingOutputWriter.close();
        nonOverlappingOutputWriter.flush();
        nonOverlappingOutputWriter.close();
    }


    private Set<String> readIpcrBarcodesAsSet() throws IOException {
        Set<String> ipcrBarcodes = new HashSet<>();

        for (String file : params.getInputIpcr()) {

            Set<String> currentBarcodes;
            switch (params.getInputType()) {
                case "INFO":
                    InfoFileReader ipcrBarcodeReader = new SparseInfoFileReader(params.getOutputPrefix());
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

        return ipcrBarcodes;
    }


    private void writeBarcodeCollection(Collection<String> output, GenericFile file) throws IOException {
        BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(file.getAsOutputStream()));

        for (String barcode : output) {
            outputWriter.write(barcode);
            outputWriter.newLine();
        }

        outputWriter.flush();
        outputWriter.close();
    }


    // TODO: Not the most efficient thing, if trimming becomes standard, will implement it in the file readers
    // For testing this will suffice
    private Collection<String> trimBarcodesFivePrime(Collection<String> input, Collection<String> output,  int trimLength) {
        for (String curBarcode : input) {
            output.add(curBarcode.substring(trimLength));
        }
        return output;
    }

}
