package nl.umcg.suresnp.pipeline.io.infofilereader;

import nl.umcg.suresnp.pipeline.inforecords.InfoRecord;
import nl.umcg.suresnp.pipeline.inforecords.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

@Deprecated
public class BarebonesInfoFileReader implements InfoFileReader {
    private static final Logger LOGGER = Logger.getLogger(BarebonesInfoFileReader.class);
    private int barcodeLengthFilter;

    public BarebonesInfoFileReader(int barcodeLengthFilter) throws IOException {
        this.barcodeLengthFilter = barcodeLengthFilter;
    }

    @Override
    public List<InfoRecord> readBarcodeFileAsList(GenericFile file) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<InfoRecord> readBarcodeFileAsList(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> readBarcodeFileAsStringMap(GenericFile file) throws IOException {
        // May seem excessive, but allows for easy change to zipped files if needed
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getAsInputStream()));
        Map<String, String> barcodeRecordMap = new HashMap<>();

        int curRecord = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            logProgress(curRecord, 1000000, "BarebonesInfoFileReader");

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
        System.out.print("\n"); // Flush progress bar
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
    public Set<String> getBarcodeSet(GenericFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getAsInputStream()));
        Set<String> barcodeSet = new HashSet<>();

        int curRecord = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            logProgress(curRecord, 1000000, "BarebonesInfoFileReader");

            String[] cols = line.split("\t");

            // Initialize filter parameters
            if (cols.length == 11) {
                String bc =  cols[4];
                if (bc.length() == barcodeLengthFilter) {
                    barcodeSet.add(bc);
                }
            }
            curRecord++;
        }
        reader.close();
        LOGGER.info("Done read: " + barcodeSet.size() + " barcodes");
        return barcodeSet;
    }

    @Override
    public Set<String> getBarcodeSet(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        throw new UnsupportedEncodingException();
    }

    @Override
    public List<String> getBarcodeList(GenericFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getAsInputStream()));
        List<String> barcodeList = new ArrayList<>();

        int curRecord = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            logProgress(curRecord, 1000000, "BarebonesInfoFileReader");

            String[] cols = line.split("\t");

            // Initialize filter parameters
            if (cols.length == 11) {
                String bc =  cols[4];
                if (bc.length() == barcodeLengthFilter) {
                    barcodeList.add(bc);
                }
            }
            curRecord++;
        }
        reader.close();
        LOGGER.info("Done read: " + barcodeList.size() + " barcodes");
        return barcodeList;    }

    @Override
    public List<String> getBarcodeList(GenericFile file, List<InfoRecordFilter> filters) throws IOException {
        throw new UnsupportedEncodingException();
    }

    @Override
    public void flushAndClose() throws IOException {
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
