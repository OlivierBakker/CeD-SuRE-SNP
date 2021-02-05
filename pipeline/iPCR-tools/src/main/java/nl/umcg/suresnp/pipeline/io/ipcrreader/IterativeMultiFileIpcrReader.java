package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecordIterator;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class IterativeMultiFileIpcrReader implements IpcrRecordProvider, Iterable<IpcrRecord> {

    private static final Logger LOGGER = Logger.getLogger(IterativeMultiFileIpcrReader.class);
    private String[] files;
    private int currentFileIndex;
    private IpcrRecordProvider currentProvider;

    public IterativeMultiFileIpcrReader(String[] files, int currentFileIndex) {
        this.files = files;
        this.currentFileIndex = currentFileIndex;
        this.currentProvider = null;
    }

    public IterativeMultiFileIpcrReader(String[] files) throws IOException {
        this.files = files;
        this.currentFileIndex = 0;
        this.currentProvider = new IpcrFileReader(new GenericFile(files[currentFileIndex]), true);
    }

    @Override
    public IpcrRecord getNextRecord() throws IOException {

        if (currentProvider == null) {
            LOGGER.warn("Provider is null, this should not happen");
            throw new IOException("Provider is null, likely due to the file not existing");
        }

        IpcrRecord currentRecord = currentProvider.getNextRecord();
        if (readAllFiles() && currentRecord == null) {
            return null;
        }

        if (!readAllFiles() && currentRecord == null) {
            currentProvider.close();
            currentFileIndex++;
            currentProvider = new IpcrFileReader(new GenericFile(files[currentFileIndex]), true);
            currentRecord = currentProvider.getNextRecord();
        }

        return currentRecord;
    }

    @Override
    public List<IpcrRecord> getRecordsAsList() throws IOException {
        return getRecordsAsList(new ArrayList<>());
    }

    @Override
    public List<IpcrRecord> getRecordsAsList(List<IpcrRecordFilter> filters) throws IOException {

/*        long start = System.currentTimeMillis();
        List<IpcrRecord> inputIpcr = new ArrayList<>();

        for (String file : files) {
            GenericFile inputFile = new GenericFile(file);
            LOGGER.info("Reading file: " + inputFile.getBaseName());

            IpcrRecordProvider provider = new IpcrFileReader(inputFile, true);

            if (filters.size() > 0) {
                inputIpcr.addAll(provider.getRecordsAsList(filters));
            } else {
                inputIpcr.addAll(provider.getRecordsAsList());
            }
            provider.close();
        }

        long stop = System.currentTimeMillis();
        LOGGER.info("Done reading. Took: " + ((stop - start) / 1000) + " seconds");*/
        List<IpcrRecord> inputIpcr = new ArrayList<>();
        IpcrRecord curRec = getNextRecord();
        while (curRec != null) {

            boolean passesFilter = true;
            for (IpcrRecordFilter filter : filters) {
                if (!filter.passesFilter(curRec)) {
                    passesFilter = false;
                    break;
                }
            }

            if (passesFilter) {
                inputIpcr.add(curRec);
            }
            curRec = getNextRecord();
        }

        return inputIpcr;
    }

    @Override
    public Set<String> getBarcodeSet() throws IOException {
        throw new NotImplementedException("Not yet implemented");
    }

    @Override
    public String[] getCdnaSamples() {
        return currentProvider.getCdnaSamples();
    }

    @Override
    public void close() throws IOException {
        currentProvider.close();
    }

    protected boolean readAllFiles() {
        return currentFileIndex >= files.length - 1;
    }

    @Override
    public Iterator<IpcrRecord> iterator() {
        return new IpcrRecordIterator(this);
    }

    public String[] getFiles() {
        return files;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    public int getCurrentFileIndex() {
        return currentFileIndex;
    }

    public void setCurrentFileIndex(int currentFileIndex) {
        this.currentFileIndex = currentFileIndex;
    }

    public IpcrRecordProvider getCurrentProvider() {
        return currentProvider;
    }

    public void setCurrentProvider(IpcrRecordProvider currentProvider) {
        this.currentProvider = currentProvider;
    }
}
