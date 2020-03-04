package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilter;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public IpcrFileReader() {
        this.sep = "\t";
        this.hasHeader = true;
    }

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

    private void setBarcodeReader(GenericFile file) throws IOException {
        try {
            String suffix = "";
            if (file.isGZipped()) {
                suffix = ".gz";
            }

            String curPath = file.getFolder() + file.getFileName().trim().replaceFirst("\\.[b]?gz$", "") + ".barcodes" + suffix;
            LOGGER.info("Reading barcodes from: " + curPath);
            this.barcodeReader = new GenericFile(curPath).getAsBufferedReader();
        } catch (FileNotFoundException e) {
            LOGGER.warn("No barcode file found, certain functionality will be slower");
            this.barcodeReader = null;
        }
    }

    protected String getNextLine() throws IOException {
        return coreReader.readLine();
    }

    protected void setHeader() throws IOException {
        String line = getNextLine();
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
        String line = getNextLine();
        if (line != null) {
            return IpcrCodec.parseFullIpcrRecord(line, getCdnaSamples(), getSep());
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

    public String getSep() {
        return sep;
    }
}
