package nl.umcg.suresnp.pipeline.io.bedreader;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.FeatureReader;
import htsjdk.tribble.TabixFeatureReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BlockCompressedIpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrCodec;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class BlockCompressedGenomicAnnotationReader implements GenomicAnnotationProvider {

    private static final Logger LOGGER = Logger.getLogger(BlockCompressedIpcrFileReader.class);
    private static String sep = "\t";
    private String[] header;

    private BlockCompressedInputStream coreInputStream;
    private FeatureReader<GenericGenomicAnnotationRecord> featureReader;
    private List<Locatable> intervals;
    private int index;
    private CloseableTribbleIterator<GenericGenomicAnnotationRecord> iteratorCache;

    public BlockCompressedGenomicAnnotationReader(GenericFile inputFile, List<Locatable> intervals, boolean hasHeader) throws IOException {

        this.coreInputStream = new BlockCompressedInputStream(new File(inputFile.getPathAsString()));

        // Set the header
        if (hasHeader) {
            header = coreInputStream.readLine().split(sep);
        } else {
            header = new String[coreInputStream.readLine().split(sep).length];
            for (int i = 0; i < header.length; i++) {
                header[i] = inputFile.getBaseName() + "_" + i;
            }
            // Reset the reader
            this.coreInputStream.reset();
        }

        // Set feature reader
        setFeatureReader(inputFile, hasHeader);

        // Intervals to loop over
        this.intervals = intervals;
        this.index = 0;

    }

    private void setFeatureReader(GenericFile inputFile, boolean hasHeader) throws FileNotFoundException {
        if (inputFile.isTabixIndexed()) {
            featureReader = TabixFeatureReader.getFeatureReader(
                    inputFile.getPathAsString(),
                    inputFile.getTabixFile().getPathAsString(),
                    new GenericGenomicAnnotationCodec(hasHeader),
                    true);
        } else {
            LOGGER.warn("Tabix index not found for: " + inputFile.getPath());
            LOGGER.warn("Querying on position will not work. Please index file first using tabix");
            featureReader = null;
        }
    }

    @Override
    public boolean hasNext() {
        if (index < (intervals.size() - 1)) {
            return true;
        } else if (index == (intervals.size() - 1)) {
            return iteratorCache.hasNext();
        }

        return false;
    }

    @Override
    public GenericGenomicAnnotationRecord next() {
        if (hasNext()) {
            if (iteratorCache !=null && !iteratorCache.hasNext()) {
                iteratorCache.close();
                iteratorCache = null;
            }

            if (iteratorCache == null) {
                iteratorCache = query(intervals.get(index));
                index++;
            }

            return iteratorCache.next();
        } else {
            return null;
        }

    }

    @Override
    public void close() throws IOException {
        featureReader.close();
        coreInputStream.close();
    }

    @Override
    public Iterator<GenericGenomicAnnotationRecord> iterator() {
        return this;
    }

    @Override
    public List<BedRecord> getBedRecordAsList() throws IOException {
        final List<BedRecord> genericGenomicAnnotationsAsList = (List<BedRecord>) (List<?>) this.getGenericGenomicAnnotationsAsList();
        return genericGenomicAnnotationsAsList;
    }

    @Override
    public List<GenericGenomicAnnotationRecord> getGenericGenomicAnnotationsAsList() throws IOException {
        List<GenericGenomicAnnotationRecord> output = new TreeList<>();
        int i = 0;

        while (hasNext()) {
            logProgress(i, 10000, "BlockCompressedGenomicAnnotationReader");
            i++;
            GenericGenomicAnnotationRecord curRecord = next();
            if (curRecord != null) {
                output.add(curRecord);
            }
        }
        LOGGER.info("Read " + output.size() + " records");
        return output;
    }

    @Override
    public String[] getHeader() {
        return header;
    }

    public CloseableTribbleIterator<GenericGenomicAnnotationRecord> query(Locatable interval) {
        try {
            return featureReader.query(interval);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
