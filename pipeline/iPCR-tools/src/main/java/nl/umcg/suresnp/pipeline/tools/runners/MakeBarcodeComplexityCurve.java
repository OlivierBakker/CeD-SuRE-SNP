package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.inforecord.consumers.BarcodeConsumer;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeBarcodeComplexityCurveParameters;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class MakeBarcodeComplexityCurve {

    private static final Logger LOGGER = Logger.getLogger(MakeBarcodeComplexityCurve.class);
    private MakeBarcodeComplexityCurveParameters params;
    private OutputStream outputStream;
    private BufferedWriter writer;

    public MakeBarcodeComplexityCurve(MakeBarcodeComplexityCurveParameters params) throws IOException {
        this.params = params;
        this.outputStream = new BufferedOutputStream(new FileOutputStream(params.getOutputPrefix() + ".uniqueBarcodeCounts"));
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public void run() throws IOException {


        // Read barcode file
        List<String> barcodeList = readBarcodes();
        int[] intervals;

        LOGGER.debug("Number of barcodes: " + barcodeList.size());

        if (params.getIntervalMax() > 0) {
            intervals = generateIntervals(0, params.getIntervalMax(), params.getnDownSampleIntervals());
        } else {
            intervals = generateIntervals(0, barcodeList.size(), params.getnDownSampleIntervals());
        }
        LOGGER.info("Calculating unique barcode counts for following intervals: " + Arrays.toString(intervals));


        // Write header line
        writer.write("iteration");
        for (int interval : intervals) {
            writer.write("\t" + interval);
        }
        writer.newLine();

        //Set<String> tmp = new HashSet<>(barcodeList);
        //LOGGER.info("Number of unique barcodes: " + tmp.size());

        // Calculate # of unique barcodes
        for (int i = 0; i < params.getnIterations(); i++) {
            writer.write("iteration" + i);
            for (int randomReadCount : intervals) {
                LOGGER.info("Running iteration: " + i + " interval: " + randomReadCount);

                if (randomReadCount == barcodeList.size()) {
                    writer.write("\t" + new HashSet<>(barcodeList).size());
                } else {

                    // Replaced this with the old code again, as this was way less efficient. I guess due to overhead
                    // it scaled poorly with N eventough all actions should be O(1)
                    // Also had much poorer memory overhead
                    // Sample a random barcode without replacement
                    // Computation time went from 30min to 8.30 min, and from 40G to about 16G
                    //BarcodeConsumer consumer = new BarcodeConsumer(new HashSet<>(), barcodeList);
                    //new Random().ints(0, barcodeList.size()).distinct().limit(randomReadCount).forEach(consumer);
                    //writer.write("\t" + consumer.getSize());

                    HashSet<String> uniqueBarcodes = pickNRandomElementsAsSet(barcodeList, randomReadCount);
                    writer.write("\t" + uniqueBarcodes.size());

                    writer.flush();
                }

            }
            writer.newLine();
            writer.flush();
        }

        writer.flush();
        writer.close();
        outputStream.flush();
        outputStream.close();

        LOGGER.debug("Debug point");
    }


    private List<String> readBarcodes() throws IOException {

        List<String> barcodeList = new ArrayList<>(params.getIntervalMax());

        // Read barcode data
        BufferedReader reader = new GenericFile(params.getInputBarcodes()).getAsBufferedReader();

        int curRecord = 0;
        int savedRecods = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            logProgress(curRecord, 1000000, "MakeBarcodeComplexityCurve");

            String bc = null;
            if (params.isSimpleReader()) {
                bc = line;
            } else {
                String[] cols = line.split("\t");
                // Initialize filter parameters
                if (cols.length == 11) {
                    bc = cols[4];
                } else {
                    continue;
                }
            }

            if (bc.length() == params.getBarcodeLength()) {

                if (params.getIntervalMax() > 0) {
                    if (savedRecods < params.getIntervalMax()) {
                        barcodeList.add(bc);
                        savedRecods++;
                    } else {
                        LOGGER.info("Read " + barcodeList.size() + " records");
                        break;
                    }
                } else {
                    barcodeList.add(bc);
                    savedRecods++;
                }

            }

            curRecord++;
        }
        System.out.print("\n"); // Flush progress bar
        LOGGER.info("Read " + barcodeList.size() + " records");
        reader.close();


        return barcodeList;
    }

    private static int[] generateIntervals(int start, int end, int nValues) {

/*        int interval = Math.round((end - start) / nValues);
        int[] out = new int[nValues];
        int j = 0;
        for (int i = start + interval; i <= end || j <= nValues - 1; i += interval) {
            out[j] = i;
            j++;
        }*/

        int stepSize = Math.round((end - start) / (float) nValues);
        int[] out = new int[nValues];

        for(int j=0; j < nValues; j++) {
            out[j] = start + (stepSize * (j+1));
        }

        if (out[nValues-1] > end) {
            out[nValues-1] = end-1;
        }

        return out;
    }
    /*public void run() throws IOException {

        // Read barcode data
        List<InfoRecordFilter> filters = new ArrayList<>();
        filters.add(new FivePrimeFragmentLengthEqualsFilter(params.getBarcodeLength()));
        filters.add(new AdapterSequenceMaxMismatchFilter(params.getAdapterMaxMismatch()));
        Map<String, String> barcodes = barcodeFileReader.readBarcodeFileAsStringMap(new GenericFile(params.getInputBarcodes()), filters);

        int[] intervals = generateIntervals(0, barcodes.size(), params.getnDownSampleIntervals());
        LOGGER.info("Calculating unique barcode counts for following intervals: " + Arrays.toString(intervals));

        writer.write("iteration" );
        for (int interval : intervals) {
            writer.write("\t" + interval );
        }
        writer.newLine();

        for (int i=0; i < params.getnIterations(); i++) {
            LOGGER.info("Running iteration: " + i);

            writer.write("iteration" + i);
            for (int randomReadCount : intervals) {
                Set<String> currentUniqueBarcodes = new HashSet<>();

                for(String currentRead : pickNRandomElements(new ArrayList<>(barcodes.keySet()), randomReadCount)) {
                    currentUniqueBarcodes.add(barcodes.get(currentRead));
                }
                writer.write("\t" + currentUniqueBarcodes.size());

            }
            writer.newLine();
            writer.flush();
        }

        writer.flush();
        writer.close();
        outputStream.flush();
        outputStream.close();

        LOGGER.debug("Debug point");
    }

    private static int[] generateIntervals(int start, int end, int nValues) {

        int interval = Math.round((end - start) / nValues);
        int[] out = new int[nValues];
        int j = 0;
        for(int i=start + interval; i <= end || j <= nValues -1 ; i+=interval) {
            out[j] = i;
            j++;
        }


        return out;
    }*/

    // Credit to Kostas Chalkias: https://stackoverflow.com/questions/4702036/take-n-random-elements-from-a-liste
    public static <E> List<E> pickNRandomElements(List<E> list, int n, Random r) {
        int length = list.size();

        if (length < n) return null;

        // We don't need to shuffle the whole list
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    // Credit to Kostas Chalkias: https://stackoverflow.com/questions/4702036/take-n-random-elements-from-a-liste
    public static <E> List<E> pickNRandomElements(List<E> list, int n) {
        return pickNRandomElements(list, n, ThreadLocalRandom.current());
    }

    public static HashSet<String> pickNRandomElementsAsSet(List<String> list, int n) {
        Random r = ThreadLocalRandom.current();
        int length = list.size();

        if (length < n) return null;

        // We don't need to shuffle the whole list
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
        return new HashSet<>(list.subList(length - n, length));
    }



}
