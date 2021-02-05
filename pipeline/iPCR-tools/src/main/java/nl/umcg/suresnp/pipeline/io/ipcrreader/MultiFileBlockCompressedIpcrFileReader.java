package nl.umcg.suresnp.pipeline.io.ipcrreader;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTree;
import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
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

public class MultiFileBlockCompressedIpcrFileReader extends IterativeMultiFileIpcrReader implements IpcrRecordProvider, Iterable<IpcrRecord> {

    private static final Logger LOGGER = Logger.getLogger(IterativeMultiFileIpcrReader.class);
    private List<BlockCompressedIpcrFileReader> providers;

    public MultiFileBlockCompressedIpcrFileReader(String[] files) throws IOException {
        super(files, 0);

        this.providers = new ArrayList<>();
        for (String file : files) {
            this.providers.add(new BlockCompressedIpcrFileReader(new GenericFile(file)));
        }
        this.setCurrentProvider(providers.get(this.getCurrentFileIndex()));
    }

    @Override
    public IpcrRecord getNextRecord() throws IOException {

        if (getCurrentProvider() == null) {
            LOGGER.warn("Provider is null, this should not happen");
            throw new IOException("Provider is null, likely due to the file not existing");
        }

        IpcrRecord currentRecord = getCurrentProvider().getNextRecord();
        if (readAllFiles() && currentRecord == null) {
            return null;
        }

        if (!readAllFiles() && currentRecord == null) {
            getCurrentProvider().close();
            setCurrentFileIndex(getCurrentFileIndex() + 1);
            setCurrentProvider(providers.get(getCurrentFileIndex()));
            currentRecord = getCurrentProvider().getNextRecord();
        }

        return currentRecord;
    }

    public List<IpcrRecord> queryAsList(String contig, int start, int end) throws IOException {

        List<IpcrRecord> output = new ArrayList<>();
        for (int i=0; i < getFiles().length; i++) {
            BlockCompressedIpcrFileReader featureReader = providers.get(i);
            for (IpcrRecord rec : featureReader.query(contig, start, end)) {
                output.add(rec);
            }
        }
        return output;
    }

    public IntervalTreeMap<IpcrRecord> queryAsIntervalTreeMap(String contig, int start, int end) throws IOException {
        IntervalTreeMap<IpcrRecord> output = new IntervalTreeMap<>();
        for (int i=0; i < getFiles().length; i++) {
            BlockCompressedIpcrFileReader featureReader = providers.get(i);
            for (IpcrRecord rec : featureReader.query(contig, start, end)) {
                output.put(new BedRecord(rec), rec);
            }
        }
        return output;
    }

    public Integer queryOverlapSize(String contig, int start, int end) throws IOException {
        int output = 0;

        for (int i=0; i < getFiles().length; i++) {
            BlockCompressedIpcrFileReader featureReader = providers.get(i);
            output += featureReader.query(contig, start, end).stream().count();
        }
        return output;
    }

    public Integer queryOverlapSizeNonZeroCdna(String contig, int start, int end, AdaptableScoreProvider provider) throws IOException {
        int output = 0;

        for (int i=0; i < getFiles().length; i++) {
            BlockCompressedIpcrFileReader featureReader = providers.get(i);
            for (IpcrRecord curRecord : featureReader.query(contig, start, end)) {
                if (provider.getScore(curRecord) > 0) {
                    output ++;
                }
            }

        }
        return output;
    }
}
