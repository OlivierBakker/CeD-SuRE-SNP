package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.barcodes.filters.AdapterSequenceMaxMismatchFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.BarcodeFileReader;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.BarebonesBarcodeFileReader;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateBarcodeComplexityCurve {


    private static final Logger LOGGER = Logger.getLogger(GenerateBarcodeComplexityCurve.class);
    private GenerateBarcodeComplexityCurveParameters params;
    private BarcodeFileReader barcodeFileReader;
    private OutputStream outputStream;
    private BufferedWriter writer;

    public GenerateBarcodeComplexityCurve(GenerateBarcodeComplexityCurveParameters params) throws IOException {
        this.params = params;
        this.barcodeFileReader = new BarebonesBarcodeFileReader(params.getBarcodeLength());
        this.outputStream = new BufferedOutputStream(new FileOutputStream(params.getOutputPrefix() + ".uniqueBarcodeCounts"));
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }


    public void run() throws IOException {

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
    }

    // Credit to Kostas Chalkias: https://stackoverflow.com/questions/4702036/take-n-random-elements-from-a-liste
    public static <E> List<E> pickNRandomElements(List<E> list, int n, Random r) {
        int length = list.size();

        if (length < n) return null;

        // We don't need to shuffle the whole list
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i , r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    // Credit to Kostas Chalkias: https://stackoverflow.com/questions/4702036/take-n-random-elements-from-a-liste
    public static <E> List<E> pickNRandomElements(List<E> list, int n) {
        return pickNRandomElements(list, n, ThreadLocalRandom.current());
    }


}
