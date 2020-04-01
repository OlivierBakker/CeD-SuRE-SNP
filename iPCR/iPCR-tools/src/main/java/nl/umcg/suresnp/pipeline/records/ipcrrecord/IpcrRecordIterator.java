package nl.umcg.suresnp.pipeline.records.ipcrrecord;

import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;

import java.io.IOException;
import java.util.Iterator;

public class IpcrRecordIterator implements Iterator<IpcrRecord> {

    private IpcrRecord cachedRecord;
    private IpcrRecordProvider provider;

    public IpcrRecordIterator(IpcrRecordProvider provider) {
        this.provider = provider;
        try {
            cachedRecord = provider.getNextRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {
        return cachedRecord != null;
    }

    @Override
    public IpcrRecord next() {
        IpcrRecord current = cachedRecord;
        try {
            cachedRecord = provider.getNextRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return current;
    }
}
