package nl.umcg.suresnp.pipeline.io.ipcrreader;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.FeatureReader;
import htsjdk.tribble.TabixFeatureReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecordIterator;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.InRegionFilter;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;


public class BlockCompressedIpcrFileReader extends IpcrFileReader implements IpcrRecordProvider, Iterable<IpcrRecord> {

    private static final Logger LOGGER = Logger.getLogger(BlockCompressedIpcrFileReader.class);
    private BlockCompressedInputStream coreInputStream;
    private BufferedReader barcodeReader;
    private String sep;
    private FeatureReader<IpcrRecord> featureReader;

    // For iterating over tribble indexed regions
    private InRegionFilter regions;
    private CloseableTribbleIterator<IpcrRecord> cachedIterator;
    private BedRecord cachedRegion;

    public BlockCompressedIpcrFileReader(GenericFile file) throws IOException {
        this.coreInputStream = new BlockCompressedInputStream(new File(file.getPathAsString()));
        this.sep = "\t";
        setBarcodeReader(file);
        setHeader();
        setFeatureReader(file.getPathAsString());

        this.regions = null;
        this.cachedIterator = null;
        this.cachedRegion = null;
    }

    public BlockCompressedIpcrFileReader(GenericFile file, InRegionFilter regions) throws IOException {
        this.regions = regions;
        this.coreInputStream = new BlockCompressedInputStream(new File(file.getPathAsString()));
        this.sep = "\t";
        setBarcodeReader(file);
        setHeader();
        setFeatureReader(file.getPathAsString());
    }

    private void setFeatureReader(String file) throws FileNotFoundException {
        if (new File(file + ".tbi").exists()) {
            featureReader = TabixFeatureReader.getFeatureReader(
                    file,
                    file + ".tbi",
                    new IpcrCodec(),
                    true);
        } else {
            LOGGER.warn("Tabix index not found for: " + file);
            LOGGER.warn("Querying on position will not work. Please index file first using -T Index or -T Recode -s");
            featureReader = null;
        }
    }

    @Override
    protected String getNextLine() throws IOException {
        return coreInputStream.readLine();
    }

    @Override
    public IpcrRecord getNextRecord() throws IOException {
        // If no regions provided, just iterate over all items using default impl
        if (regions == null) {
            return super.getNextRecord();
        } else {

            // For the first iter
            if (cachedRegion == null && regions.hasNext()) {
                cachedRegion = regions.next();
            }

            if (cachedIterator == null || !cachedIterator.hasNext()) {
                cachedIterator = this.query(cachedRegion.getContig(), cachedRegion.getStart(), cachedRegion.getEnd());
                cachedRegion = regions.next();
            }

            return cachedIterator.next();
        }
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
            String line = coreInputStream.readLine();
            while (line != null) {
                logProgress(i, 1000000, "IpcrFileReader");
                i++;

                String[] data = line.split(sep);
                barcodeSet.add(data[0]);
                line = coreInputStream.readLine();
            }
        }

        LOGGER.info("Read " + i + " records");
        return barcodeSet;
    }

    @Override
    public void close() throws IOException {
        coreInputStream.close();
        if (featureReader != null) {
            featureReader.close();
        }
    }

    public CloseableTribbleIterator<IpcrRecord> query(String contig, int start, int end) throws IOException {
        if (featureReader == null) {
            throw new IllegalStateException("Feature reader is null, Have you indexed the iPCR file?");
        }
        return featureReader.query(contig, start, end);
    }

    public List<IpcrRecord> queryAsList(String contig, int start, int end) throws IOException {

        if (featureReader == null) {
            throw new IllegalStateException("Feature reader is null, Have you indexed the iPCR file?");
        }
        List<IpcrRecord> output = new ArrayList<>();
        for (IpcrRecord rec : featureReader.query(contig, start, end)) {
            output.add(rec);
        }
        return output;
    }

    public Map<String, IpcrRecord> queryAsMap(String contig, int start, int end) throws IOException {

        if (featureReader == null) {
            throw new IllegalStateException("Feature reader is null, Have you indexed the iPCR file?");
        }
        Map<String, IpcrRecord> output = new HashMap<>();
        for (IpcrRecord rec : featureReader.query(contig, start, end)) {
            output.put(rec.getPrimaryReadName(), rec);
        }
        return output;
    }

    public long getFilePointer() {
        return coreInputStream.getFilePointer();
    }

    private void setBarcodeReader(GenericFile file) throws IOException {
        try {
            String suffix = "";
            if (file.isGZipped()) {
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

    @Override
    public Iterator<IpcrRecord> iterator() {
        return new IpcrRecordIterator(this);
    }
}
