package nl.umcg.suresnp.pipeline.io.barcodefilereader;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarebonesBarcodeFileReader implements BarcodeFileReader {
    private static final Logger LOGGER = Logger.getLogger(BarebonesBarcodeFileReader.class);
    private int barcodeLengthFilter;

    public BarebonesBarcodeFileReader(int barcodeLengthFilter) throws IOException {
        this.barcodeLengthFilter = barcodeLengthFilter;
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFile(GenericFile file) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFile(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, String> readBarcodeFileAsStringMap(GenericFile file) throws IOException {
        // May seem excessive, but allows for easy change to zipped files if needed
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getAsInputStream()));

        Map<String, String> barcodeRecordMap = new HashMap<>();

        int curRecord = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (curRecord > 0) {
                if (curRecord % 1000000 == 0) {
                    LOGGER.info("Read " + curRecord / 1000000 + " million records");
                }
            }
            String[] cols = line.split("\t");
            // Initialize filter parameters
            if (cols.length == 11) {
                String bc =  cols[4];
                if (bc.length() == barcodeLengthFilter) {
                    barcodeRecordMap.put(cols[0].split(" ")[0], bc);
                }
            }
            curRecord++;
        }
        reader.close();
        LOGGER.info("Done read: " + barcodeRecordMap.size() + " read barcode records");
        return barcodeRecordMap;
    }

    @Override
    public Map<String, String> readBarcodeFileAsStringMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        LOGGER.warn("Filters provided, but this implementation ignores them. For proper implementation use GenericBarcodeFileReader");
        return readBarcodeFileAsStringMap(file);
    }

    @Override
    public Map<String, Integer> readBarcodeCountFile(GenericFile file) throws IOException {
        LOGGER.warn("Not implemented in BarebonesBarcodeFileReader. Returning null");
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    private static InfoRecord parseBarcodeRecord(String[] line) throws NumberFormatException {
        InfoRecord curRec = new InfoRecord(line[0].split(" ")[0],
                Integer.parseInt(line[1]),
                Integer.parseInt(line[2]),
                Integer.parseInt(line[3]),
                line[4], line[8], line[5], line[9], line[6], line[10]);
        return curRec;

    }

}
