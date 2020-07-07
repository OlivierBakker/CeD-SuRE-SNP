package nl.umcg.suresnp.pipeline.tools.runners;

import com.itextpdf.text.DocumentException;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.tabix.TabixIndexCreator;
import htsjdk.tribble.util.LittleEndianOutputStream;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.bedreader.BedRecordProvider;
import nl.umcg.suresnp.pipeline.io.bedreader.FourColBedFileReader;
import nl.umcg.suresnp.pipeline.io.bedreader.NarrowPeakReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.GenericInfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.InfoFileReader;
import nl.umcg.suresnp.pipeline.io.infofilereader.SparseInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BlockCompressedIpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.BlockCompressedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeSummariesParameters;
import nl.umcg.suresnp.pipeline.utils.BedUtils;
import nl.umcg.suresnp.pipeline.utils.StreamingHistogram;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import umcg.genetica.graphics.Grid;
import umcg.genetica.graphics.panels.ScatterplotPanel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

/**
 * The type Make summaries. Collection of small utillities / tools taking iPCR files as an input.
 */
public class MakeSummaries {

    private static final Logger LOGGER = Logger.getLogger(MakeSummaries.class);
    private static MakeSummariesParameters params;

    /**
     * Instantiates a new Make summaries.
     *
     * @param parameters the parameters
     */
    public MakeSummaries(MakeSummariesParameters parameters) {
        this.params = parameters;
    }

    /**
     * Overlap two sets of barcodes and report the count. Can take INFO or IPCR files as input
     *
     * @throws IOException the io exception
     */
    public void barcodeOverlap() throws IOException {

        // How many of the cDNA barcodes come back in the iPCR
        Map<String, Integer> cdnaBarcodeCounts = GenericInfoFileReader.readBarcodeCountFile(new GenericFile(params.getInputBarcodes()), params.getTrimBarcodesToLength());
        Set<String> ipcrBarcodes = readIpcrBarcodesAsSet(params.getTrimBarcodesToLength());

        if (params.getTrimBarcodesToLength() > 0) {
            LOGGER.info("Trimming barcodes to length: " + params.getTrimBarcodesToLength());
        }

        int indexNumber = 100;
        int inputIpcrCount = ipcrBarcodes.size();
        int inputCdnaCount = cdnaBarcodeCounts.size();

        // LOGGER.info("Trimming barcode sets");
        //cdnaBarcodes = (Set<String>) trimBarcodesFivePrime(cdnaBarcodes, new HashSet<>(), 2);
        //ipcrBarcodes = (Set<String>) trimBarcodesFivePrime(ipcrBarcodes, new HashSet<>(), 2);

        Set<String> overlappingCdnaBarcodes = new HashSet<>(cdnaBarcodeCounts.keySet());
        overlappingCdnaBarcodes.retainAll(ipcrBarcodes);
        int overlappingCount = overlappingCdnaBarcodes.size();

        LOGGER.info("cDNA: " + inputCdnaCount);
        LOGGER.info("iPCR: " + inputIpcrCount);
        LOGGER.info("Overlap: " + overlappingCount);

        // TODO: very very ugly improve
        int totalCdnaCount = 0;
        int barcodeGreaterThan50 = 0;
        int barcodeGreaterThan100 = 0;
        int barcodeGreaterThan250 = 0;
        int barcodeGreaterThan500 = 0;
        int barcodeGreaterThan1000 = 0;
        int[] allUniqueCounts = new int[indexNumber];

        for (String key : cdnaBarcodeCounts.keySet()) {
            int curCount = cdnaBarcodeCounts.get(key);
            for (int i = 0; i < indexNumber; i++) {
                if (curCount == i) {
                    allUniqueCounts[i]++;
                }
            }
            totalCdnaCount += curCount;

            if (curCount > 50) {
                barcodeGreaterThan50 += curCount;
            }
            if (curCount > 100) {
                barcodeGreaterThan100 += curCount;
            }
            if (curCount > 250) {
                barcodeGreaterThan250 += curCount;
            }
            if (curCount > 500) {
                barcodeGreaterThan500 += curCount;
            }
            if (curCount > 1000) {
                barcodeGreaterThan1000 += curCount;
            }
        }

        // Unique counts per bin
        int[] overlappingUniqueCounts = new int[indexNumber];
        for (String key : overlappingCdnaBarcodes) {
            int curCount = cdnaBarcodeCounts.get(key);
            for (int i = 0; i < indexNumber; i++) {
                if (curCount == i) {
                    overlappingUniqueCounts[i]++;
                }
            }
        }

        // Percentage unique per bin
        double[] overlappingPercentages = new double[indexNumber];
        for (int i = 0; i < indexNumber; i++) {
            overlappingPercentages[i] = ((double) overlappingUniqueCounts[i] / (double) allUniqueCounts[i]) * 100;
        }

        LOGGER.info(inputCdnaCount +
                " cDNA barcodes  of which " +
                overlappingCount +
                " (" + ((double) overlappingCount / (double) inputCdnaCount) * 100 + "%) could be found in iPCR");

        // Summary file overall
        BufferedWriter output = new GenericFile(params.getOutputPrefix() + ".barcodeOverlap.tsv").getAsBufferedWriter();
        output.write("total_cDNA\tunique_cDNA\tunique_iPCR\toverlap\tperc_overlap\t50\t100\t250\t500\t1000\n");
        output.write(totalCdnaCount + "\t" +
                inputCdnaCount + "\t" +
                inputIpcrCount + "\t" +
                overlappingCount + "\t" +
                ((double) overlappingCount / (double) inputCdnaCount) * 100 + "\t" +
                barcodeGreaterThan50 + "\t" +
                barcodeGreaterThan100 + "\t" +
                barcodeGreaterThan250 + "\t" +
                barcodeGreaterThan500 + "\t" +
                barcodeGreaterThan1000
        );
        output.newLine();
        output.flush();
        output.close();


        // Overlapping counts per bin
        output = new GenericFile(params.getOutputPrefix() + ".barcodeOverlapPerBin.tsv").getAsBufferedWriter();

        for (int i : allUniqueCounts) {
            output.write(i + "\t");
        }
        output.newLine();

        for (int i : overlappingUniqueCounts) {
            output.write(i + "\t");
        }
        output.newLine();

        for (double i : overlappingPercentages) {
            output.write(i + "\t");
        }
        output.newLine();

        output.flush();
        output.close();
    }


    /**
     * Calculate the insert size of iPCR fragments. Takes IPCR file as input. Prints histogram.
     * TODO: add output saving to file and plotting of hist.
     *
     * @throws IOException              the io exception
     * @throws IllegalArgumentException the illegal argument exception
     */
    public void getInsertSizes() throws IOException, IllegalArgumentException {

        long insertSizeTotal = 0;
        long totalIpcrCount = 0;

        BufferedWriter histogramWriter = new GenericFile(params.getOutputPrefix() + ".insert.size.histogram.tsv").getAsBufferedWriter();
        BufferedWriter outputWriter = new GenericFile(params.getOutputPrefix() + ".insert.size.tsv").getAsBufferedWriter();
        outputWriter.write("source\ttotalCount\tmean\tapproxMedian\n");

        StreamingHistogram hist = new StreamingHistogram(1, 500);

        for (String file : params.getInputIpcr()) {
            switch (params.getInputType()) {
                case "IPCR":
                    IpcrRecordProvider ipcrRecordProvider = new IpcrFileReader(new GenericFile(file), true);
                    IpcrRecord curRecord = ipcrRecordProvider.getNextRecord();
                    long curRecordCount = 0;
                    long curInsertSizeTotal = 0;

                    // Little bit ugly, but works to get median value
                    StreamingHistogram curHist = new StreamingHistogram(1, 500);

                    while (curRecord != null) {
                        logProgress(curRecordCount, 1000000, "MakeSummaries");

                        int curInsertSize = curRecord.getOrientationIndependentEnd() - curRecord.getOrientationIndependentStart();
                        hist.addPostiveValue(curInsertSize);
                        curHist.addPostiveValue(curInsertSize);

                        insertSizeTotal = insertSizeTotal + curInsertSize;
                        curInsertSizeTotal = curInsertSizeTotal + curInsertSize;

                        curRecordCount++;
                        totalIpcrCount++;

                        curRecord = ipcrRecordProvider.getNextRecord();
                    }
                    System.out.print("\n"); // Flush progress bar
                    LOGGER.info("Mean for: " + file + "\t" + Math.round((double) curInsertSizeTotal / (double) curRecordCount));

                    outputWriter.write(new GenericFile(file).getBaseName());
                    outputWriter.write("\t");
                    outputWriter.write(Long.toString(curRecordCount));
                    outputWriter.write("\t");
                    outputWriter.write(Double.toString(curHist.getMean()));
                    outputWriter.write("\t");
                    outputWriter.write(Double.toString(curHist.getValueOfMedianBin()));
                    outputWriter.write("\n");
                    break;

                default:
                    throw new IllegalArgumentException("Insert Sizes only supports IPCR input");
            }
        }

        LOGGER.info("Average insert size: " + hist.getMean());
        LOGGER.info("Median insert size: " + hist.getValueOfMedianBin());
        LOGGER.info("Histogram:");

        System.out.print(hist.getHistAsString());

        histogramWriter.write(hist.getHistAsTsv());
        histogramWriter.flush();
        histogramWriter.close();

        outputWriter.write("total");
        outputWriter.write("\t");
        outputWriter.write(Long.toString(totalIpcrCount));
        outputWriter.write("\t");
        outputWriter.write(Double.toString(hist.getMean()));
        outputWriter.write("\t");
        outputWriter.write(Double.toString(hist.getValueOfMedianBin()));
        outputWriter.write("\n");

        outputWriter.flush();
        outputWriter.close();

        LOGGER.info("done");
    }

    /**
     * Make a histogram of barcode counts. Takes cDNA count data as input.
     * TODO: add output saving to file and plotting of hist.
     *
     * @throws IOException the io exception
     */
    public void makeBarcodeCountHist() throws IOException {
        if (params.getTrimBarcodesToLength() > 0) {
            LOGGER.info("Trimming barcodes to length: " + params.getTrimBarcodesToLength());
        }
        Map<String, Integer> barcodeCounts = GenericInfoFileReader.readBarcodeCountFile(new GenericFile(params.getInputIpcr()[0]), params.getTrimBarcodesToLength());
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

    /**
     * Correlate two cDNA profiles. Takes cDNA data as input.
     * TODO: Cleanup and implement proper filenames and better plotting
     *
     * @throws IOException       the io exception
     * @throws DocumentException the document exception
     */
    public void getCdnaCorrelations() throws IOException, DocumentException {

        List<Map<String, Integer>> input = new ArrayList<>(params.getInputIpcr().length);
        Set<String> overlappingBarcodes = new HashSet<>();
        int i = 0;
        for (String file : params.getInputIpcr()) {
            Map<String, Integer> barcodeCounts = GenericInfoFileReader.readBarcodeCountFile(new GenericFile(file));

            if (i == 0) {
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
        double[][] x = new double[params.getInputIpcr().length][overlappingBarcodes.size()];

        i = 0;
        for (String barcode : overlappingBarcodes) {
            int j = 0;
            for (Map<String, Integer> data : input) {
                x[j][i] = Math.log((double) data.get(barcode)) / Math.log(2);
                j++;
            }
            i++;
        }

        LOGGER.info("Calculating pearson R");
        PearsonsCorrelation test = new PearsonsCorrelation();
        //RealMatrix out = test.computeCorrelationMatrix(x);
        //LOGGER.info("Pearson R: " + out.getColumn(0)[1]);
        LOGGER.info("Pearson R: " + test.correlation(x[0], x[1]));


        Grid grid = new Grid(500, 500, 1, 1, 100, 100);
        ScatterplotPanel p = new ScatterplotPanel(1, 1);
        p.setData(x[0], x[1]);
        //p.setDataRange(new Range(0, 0, 500, 500));
        p.setAlpha((float) 0.5);
        p.setLabels("X", "y");
        p.setPlotElems(true, false);
        grid.addPanel(p);
        grid.draw("output.png");

    }

    /**
     * Correlate two score profiles (from .narrowPeak or 4 col bed files)
     * TODO: Cleanup and better plotting
     *
     * @throws Exception the exception
     */
    public void getPeakCorrelations() throws Exception {
        LOGGER.info("Correlating peaks");

        // Reading the input files, can be .bed or .narrowPeak
        List<BedRecord>[] inputFiles = new List[params.getInputIpcr().length];
        int i = 0;
        for (String file : params.getInputIpcr()) {
            GenericFile curFile = new GenericFile(file);
            BedRecordProvider reader;

            LOGGER.info("Reading file: " + curFile.getFileName());
            if (curFile.getFileName().contains(".bed")) {
                reader = new FourColBedFileReader(curFile);
            } else {
                reader = new NarrowPeakReader(curFile);
            }

            inputFiles[i] = reader.getBedRecordAsList();
            reader.close();
            i++;
        }
        LOGGER.info("Done reading");

        // Intersect overlapping bed records. Remove others
        List<BedRecord>[] output = BedUtils.intersectSortedBedRecords(inputFiles[0], inputFiles[1]);
        LOGGER.info("Output " + output.length);

        // Calculate correlations and make plot
        i = 0;
        double[] x = new double[output[0].size()];
        double[] y = new double[output[1].size()];

        BufferedWriter writer = new GenericFile(params.getOutputPrefix() + ".xy.tsv").getAsBufferedWriter();
        writer.write("x\ty");
        while (i < output[0].size()) {
            // X records
            BedRecord cur = output[0].get(i);
            writer.write(Double.toString(cur.getScore()) + "\t");
            x[i] = Math.log(cur.getScore()) / Math.log(10);

            // Y records
            cur = output[1].get(i);
            writer.write(Double.toString(cur.getScore()));
            y[i] = Math.log(cur.getScore()) / Math.log(10);

            writer.newLine();
            i++;
        }
        writer.flush();
        writer.close();

        LOGGER.info("Calculating pearson R");
        PearsonsCorrelation test = new PearsonsCorrelation();
        LOGGER.info("Pearson R: " + test.correlation(x, y));

        // Make the plot
        Grid grid = new Grid(500, 500, 1, 1, 100, 100);
        ScatterplotPanel p = new ScatterplotPanel(1, 1);
        p.setData(x, y);
        //p.setDataRange(new Range(0, 0, 500, 500));
        p.setAlpha((float) 0.5);
        p.setLabels("X", "Y");
        p.setPlotElems(true, false);
        grid.addPanel(p);
        grid.draw(params.getOutputPrefix() + ".png");
    }


    /**
     * Index an existing bgzipped IPCR file.
     *
     * @throws IOException the io exception
     */
    public void indexIpcr() throws IOException {
        // https://academic.oup.com/bioinformatics/article/27/5/718/262743
        TabixIndexCreator indexCreator = new TabixIndexCreator(BlockCompressedIpcrRecordWriter.IPCR_FORMAT);

        BlockCompressedIpcrFileReader ipcrRecordProvider = new BlockCompressedIpcrFileReader(new GenericFile(params.getInputIpcr()[0]));
        long pointer = ipcrRecordProvider.getFilePointer();
        IpcrRecord record = ipcrRecordProvider.getNextRecord();

        long i = 0;
        while (record != null) {
            logProgress(i, 1000000, "IndexIpcr");
            try {
                indexCreator.addFeature(record, pointer);
                record = ipcrRecordProvider.getNextRecord();
                pointer = ipcrRecordProvider.getFilePointer();

            } catch (IllegalArgumentException e) {
                LOGGER.debug("U oh, something went wrong indexing. Pointer: " + pointer);
                LOGGER.debug(record.getBarcode() + "\t" + record.getContig() + "\t" + record.getStart() + "\t" + record.getEnd());
                throw e;
            }
            i++;
        }
        LOGGER.info("Done reading, processed " + i + "records");

        LittleEndianOutputStream indexOutputStream = new LittleEndianOutputStream(new BlockCompressedOutputStream(params.getInputIpcr()[0] + ".tbi"));
        Index index = indexCreator.finalizeIndex(ipcrRecordProvider.getFilePointer());
        index.write(indexOutputStream);

        indexOutputStream.flush();
        indexOutputStream.close();
        ipcrRecordProvider.close();

        LOGGER.debug("Done indexing");

    }

    /**
     * Reads all provided IPCR of INFO files and reads their barcodes into a set.
     *
     * @param trimBarcodesToLength if zero do not trim barcodes, else trim barcodes to specified length, trims bases from
     *            left side (5' end) of string.
     * @return Set of unique barcodes
     * @throws IOException the io exception
     */
    private Set<String> readIpcrBarcodesAsSet(int trimBarcodesToLength) throws IOException {
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

            if (trimBarcodesToLength == 0) {
                ipcrBarcodes.addAll(currentBarcodes);
            } else {
                for (String barcode : currentBarcodes) {
                    // Inclusive so this works correctly
                    ipcrBarcodes.add(barcode.substring(barcode.length() - trimBarcodesToLength, barcode.length()));
                }
            }

        }

        return ipcrBarcodes;
    }

}
