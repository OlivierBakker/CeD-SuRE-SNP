package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.BasicIpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilter;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class IpcrFileReader implements IpcrRecordProvider {

    private static final Logger LOGGER = Logger.getLogger(IpcrFileReader.class);

    private InputStream coreInputStream;
    private BufferedReader coreReader;
    private BufferedReader barcodeReader;
    private String sep;
    private String[] header;
    private String[] cdnaSamples;
    private boolean hasHeader;

    public IpcrFileReader(boolean hasHeader) {
        this.sep = "\t";
        this.hasHeader = hasHeader;
    }

    public IpcrFileReader(GenericFile file, boolean hasHeader) throws IOException {
        setBarcodeReader(file);
        this.coreInputStream = file.getAsInputStream();
        this.coreReader = new BufferedReader(new InputStreamReader(coreInputStream));
        this.sep = "\t";
        this.hasHeader = hasHeader;
        if (hasHeader) {
            setHeader();
        }
    }

    public IpcrFileReader(GenericFile file, boolean hasHeader, String sep) throws IOException {
        setBarcodeReader(file);
        this.coreInputStream = file.getAsInputStream();
        this.coreReader = new BufferedReader(new InputStreamReader(coreInputStream));
        this.sep = sep;
        this.hasHeader = hasHeader;
        if (hasHeader) {
            setHeader();
        }
    }

    public void setCoreInputStream(InputStream inputStream) throws IOException {
        if (this.coreReader != null) {
            LOGGER.warn("Reader was already set, closing");
            coreReader.close();
        }

        if (this.coreInputStream != null) {
            LOGGER.warn("Input stream was already set, closing");
            coreInputStream.close();
        }
        this.coreInputStream = inputStream;
        this.coreReader = new BufferedReader(new InputStreamReader(coreInputStream));
        if (hasHeader) {
            setHeader();
        }
    }


    private void setBarcodeReader(GenericFile file) throws IOException {
        try {
            String suffix = "";
            if (file.isGzipped()) {
                suffix = ".gz";
            }
            String curPath = file.getFolder() + file.getFileName().trim().replaceFirst("\\.gz$", "") + ".barcodes" + suffix;
            LOGGER.info("Reading barcodes from: " + curPath);
            this.barcodeReader = new GenericFile(curPath).getAsBufferedReader();
        } catch (FileNotFoundException e) {
            LOGGER.warn("No barcode file found, certain functionality will be slower");
            this.barcodeReader = null;
        }
    }

    private void setHeader() throws IOException {
        String line = coreReader.readLine();
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
        String line = coreReader.readLine();
        if (line != null) {
            return parseIpcrRecord(line);
        } else {
            return null;
        }
    }

    @Override
    public List<IpcrRecord> getRecordsAsList() throws IOException {
        // Uses TreeList for faster sorting
        IpcrRecord curRecord = getNextRecord();
        List<IpcrRecord> records = new TreeList<>();

        int i = 0;
        while (curRecord != null) {
            logProgress(i, 1000000, "IpcrFileReader");
            i++;
            records.add(curRecord);
            curRecord = getNextRecord();
        }
        LOGGER.info("Read " + i + " records");

        return records;
    }

    @Override
    public List<IpcrRecord> getRecordsAsList(List<IpcrRecordFilter> filters) throws IOException {
        // Uses TreeList for faster sorting
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
        LOGGER.info("Read " + i + " records");
        LOGGER.info(records.size() + " records passed filters");

        return records;
    }

    @Override
    public Set<String> getBarcodeSet() throws IOException {
        Set<String> barcodeSet = new HashSet<>();
        int i = 0;

        if (barcodeReader != null) {
            String line = barcodeReader.readLine();
            while (line != null) {
                logProgress(i, 1000000, "IpcrFileReader");
                i++;
                barcodeSet.add(line);
                line = barcodeReader.readLine();
            }
        } else {
            String line = coreReader.readLine();
            while (line != null) {
                logProgress(i, 1000000, "IpcrFileReader");
                i++;

                String[] data = line.split(sep);
                barcodeSet.add(data[0]);
                line = coreReader.readLine();
            }
        }

        LOGGER.info("Read " + i + " records");
        return barcodeSet;
    }

    @Override
    public String[] getCdnaSamples() {
        return cdnaSamples;
    }

    @Override
    public void close() throws IOException {
        coreReader.close();
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
