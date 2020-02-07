package nl.umcg.suresnp.pipeline.tools.runners;

import com.itextpdf.text.DocumentException;
import nl.umcg.suresnp.pipeline.records.inforecord.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.records.inforecord.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.GenericInfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.InfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.SparseInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeSummariesParameters;
import nl.umcg.suresnp.pipeline.utils.StreamingHistogram;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import umcg.genetica.graphics.Grid;
import umcg.genetica.graphics.panels.ScatterplotPanel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class MakeSummaries {

    // Collection of small utils to check certain things
    private static final Logger LOGGER = Logger.getLogger(MakeSummaries.class);
    private static MakeSummariesParameters params;

    public MakeSummaries(MakeSummariesParameters parameters) {
        this.params = parameters;
    }

    public void barcodeOverlap() throws IOException {
        // How many of the cDNA barcodes come back in the iPCR
        InfoFileReader cdnaBarcodeReader = new SparseInfoFileReader(params.getOutputPrefix());

        Set<String> cdnaBarcodes = new HashSet<>(GenericInfoFileReader.readBarcodeCountFile(new GenericFile(params.getInputBarcodes())).keySet());
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

    public void makeBarcodeCountHist() throws IOException {
        Map<String, Integer> barcodeCounts = GenericInfoFileReader.readBarcodeCountFile(new GenericFile(params.getInputIpcr()[0]));

        StreamingHistogram histogram = new StreamingHistogram(1, 20);

        int i = 0;
        for (int curCount : barcodeCounts.values()) {
            logProgress(i, 1000000, "MakeSummaries");
            histogram.addPostiveValue(curCount);
            i++;
        }
        LOGGER.info("");


        System.out.print(histogram.getHistAsString());
    }

    public void cdnaCorrelations() throws IOException, DocumentException {

        List<Map<String, Integer>> input = new ArrayList<>(params.getInputIpcr().length);
        Set<String> overlappingBarcodes = new HashSet<>();
        int i = 0;
        for (String file : params.getInputIpcr()) {
            Map<String, Integer> barcodeCounts = GenericInfoFileReader.readBarcodeCountFile(new GenericFile(file));

            if (i == 0 ){
                overlappingBarcodes.addAll(barcodeCounts.keySet());
            } else {
                overlappingBarcodes.retainAll(barcodeCounts.keySet());
            }

            input.add(barcodeCounts);
            i++;
        }
        LOGGER.info("Done reading, " + overlappingBarcodes.size() + " overlapping barcodes");

/*        Set<String> filteredBarcodes = new HashSet<>();
        for (String barcode : overlappingBarcodes) {
            for (Map<String, Integer> data : input) {
               if (data.get(barcode) > 3) {
                   filteredBarcodes.add(barcode);
               }
            }
        }

        LOGGER.info(filteredBarcodes.size() + " barcodes remain after filtering");*/

        // Convert to double matrix
        LOGGER.info("Converting to 2d double array");
        double[][]x = new double[params.getInputIpcr().length][overlappingBarcodes.size()];

        i = 0;
        for (String barcode : overlappingBarcodes) {
            int j=0;
            for (Map<String, Integer> data : input) {
                x[j][i] = Math.log((double)data.get(barcode)) / Math.log(2);
                j++;
            }
            i++;
        }

        LOGGER.info("Calculating pearson R");
        PearsonsCorrelation test = new PearsonsCorrelation();
        //RealMatrix out = test.computeCorrelationMatrix(x);
        //LOGGER.info("Pearson R: " + out.getColumn(0)[1]);
        LOGGER.info("Pearson R: " + test.correlation(x[0], x[1]));


        Grid grid = new Grid(500,500,1,1,100,100);
        ScatterplotPanel p = new ScatterplotPanel(1,1);
        p.setData(x[0],x[1]);
        //p.setDataRange(new Range(0, 0, 500, 500));
        p.setAlpha((float)0.5);
        p.setLabels("X", "y");
        p.setPlotElems(true, false);
        grid.addPanel(p);
        grid.draw("output.png");

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
