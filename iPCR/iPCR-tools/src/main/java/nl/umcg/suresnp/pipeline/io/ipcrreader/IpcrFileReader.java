package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.BasicIpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.filters.IpcrRecordFilter;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class IpcrFileReader implements IpcrRecordProvider {

    private static final Logger LOGGER = Logger.getLogger(IpcrFileReader.class);
    private BufferedReader reader;
    private String sep;
    private String[] header;
    private String[] cdnaSamples;

    public IpcrFileReader(GenericFile file, boolean hasHeader) throws IOException {
        reader = new BufferedReader(new InputStreamReader(file.getAsInputStream()));
        sep = "\t";
        if (hasHeader) {
            setHeader();
        }
    }

    public IpcrFileReader(GenericFile file, boolean hasHeader, String sep) throws IOException {
        reader = new BufferedReader(new InputStreamReader(file.getAsInputStream()));
        this.sep = sep;
        if (hasHeader) {
            setHeader();
        }
    }

    private void setHeader() throws IOException {
        String line = reader.readLine();
        header = line.split(sep);
        if (header.length < 16) {
            LOGGER.error("Error parsing line:");
            LOGGER.error(line);
            LOGGER.error("Needed 16 columns in IPCR file, found: " + header.length);
            throw new IllegalArgumentException("Needed 16 columns in IPCR file, found: " + header.length);
        }

        if (header.length > 16) {
            LOGGER.info("Detected barcode counts for samples:");
            cdnaSamples = new String[header.length - 16];

            int i = 16;
            int j = 0;
            while (i < header.length) {
                LOGGER.info(header[i]);
                cdnaSamples[j] = header[i];
                i++;
                j++;
            }

        }
    }

    @Override
    public IpcrRecord getNextRecord() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            return parseIpcrRecord(line);
        } else {
            return null;
        }
    }

    @Override
    public List<IpcrRecord> getRecordsAsList() throws IOException {
        IpcrRecord curRecord = getNextRecord();
        List<IpcrRecord> records = new TreeList<>();

        int i = 0;
        while (curRecord != null) {
            logProgress(i, 1000000, "IpcrFileReader");
            i++;
            records.add(curRecord);
            curRecord = getNextRecord();
        }
        System.out.print("\n"); // Flush progress bar
        LOGGER.info("Read " + i + " records");

        return records;
    }

    @Override
    public List<IpcrRecord> getRecordsAsList(List<IpcrRecordFilter> filters) throws IOException {
        IpcrRecord curRecord = getNextRecord();
        List<IpcrRecord> records = new TreeList<>();

        int i = 0;
        while (curRecord != null) {
            logProgress(i, 1000000, "IpcrFileReader");
            i++;

            for (IpcrRecordFilter filter : filters) {
                if (filter.passesFilter(curRecord)) {
                    records.add(curRecord);
                }
            }
            curRecord = getNextRecord();
        }
        System.out.print("\n"); // Flush progress bar
        LOGGER.info("Read " + i + " records");
        LOGGER.info(records.size() + " records passed filters");

        return records;
    }

    @Override
    public Set<String> getBarcodeSet() throws IOException {

        Set<String> barcodeSet = new HashSet<>();
        String line = reader.readLine();
        int i = 0;
        while (line != null) {
            logProgress(i, 1000000, "IpcrFileReader");
            i++;

            String[] data = line.split(sep);
            barcodeSet.add(data[0]);
            line = reader.readLine();
        }
        System.out.print("\n"); // Flush progress bar
        LOGGER.info("Read " + i + " records");

        return barcodeSet;
    }

    @Override
    public String[] getSamples() {
        return cdnaSamples;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private IpcrRecord parseIpcrRecord(String line) {
        String[] data = line.split(sep);
        IpcrRecord record = new BasicIpcrRecord();

        if (data.length < 16) {
            LOGGER.error("Error parsing line:");
            LOGGER.error(line);
            LOGGER.error("Needed 16 columns in IPCR file, found: " + data.length);
            throw new IllegalArgumentException("Needed 16 columns in IPCR file, found: " + data.length);
        }
        record.setBarcode(data[0]);
        record.setPrimaryReadName(data[1]);
        //record.setMateReadName(data[1]);
        record.setContig(data[2]);
        record.setPrimaryStart(Integer.parseInt(data[3]));
        record.setPrimaryEnd(Integer.parseInt(data[4]));
        record.setMateStart(Integer.parseInt(data[5]));
        record.setMateEnd(Integer.parseInt(data[6]));
        record.setPrimarySamFlags(Integer.parseInt(data[7]));
        record.setMateSamFlags(Integer.parseInt(data[8]));
        record.setPrimaryMappingQuality(Integer.parseInt(data[9]));
        record.setMateMappingQuality(Integer.parseInt(data[10]));
        record.setPrimaryCigar(data[11]);
        record.setMateCigar(data[12]);
        record.setPrimaryStrand(data[13].charAt(0));
        record.setMateStrand(data[14].charAt(0));
        record.setIpcrDuplicateCount(Integer.parseInt(data[15]));

        if (cdnaSamples != null) {
            Map<String, Integer> curBarcodeCounts = new HashMap<>();
            int i = 0;
            for (String sample : cdnaSamples) {
                curBarcodeCounts.put(sample, Integer.parseInt(data[16 + i]));
                i++;
            }
            record.setBarcodeCountPerSample(curBarcodeCounts);
        }
        return record;
    }
}
