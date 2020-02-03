package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.filters.IpcrRecordFilter;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class BinaryIpcrReader implements IpcrRecordProvider {

    private static final Logger LOGGER = Logger.getLogger(BinaryIpcrReader.class);
    private ObjectInputStream inputStream;

    // This is a bit of a fail, I was expecting the java serialization to be pretty decent, but a zipped text file
    // Is much quicker and more space efficient. If I have some spare time I might implement a custom binary format.
    public BinaryIpcrReader(GenericFile file) throws IOException {
        inputStream = new ObjectInputStream(file.getAsInputStream());
    }

    @Override
    public IpcrRecord getNextRecord() throws IOException {
        try {
            return (IpcrRecord) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Binary data does not have correct class UUID");
        } catch (EOFException e) {
            return null;
        }
    }

    @Override
    public List<IpcrRecord> getRecordsAsList() throws IOException {
        IpcrRecord curRecord = getNextRecord();
        List<IpcrRecord> records = new TreeList<>();

        int i = 0;
        while (curRecord != null) {
            logProgress(i, 1000000, "BinaryIpcrFileReader");
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
            logProgress(i, 1000000, "BinaryIpcrFileReader");
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
        IpcrRecord curRecord = getNextRecord();
        int i = 0;
        while (curRecord != null) {
            logProgress(i, 1000000, "BinaryIpcrFileReader");
            i++;
            barcodeSet.add(curRecord.getBarcode());
            curRecord = getNextRecord();
        }
        LOGGER.info("Read " + i + " records");

        return barcodeSet;
    }

    @Override
    public String[] getSamples() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
