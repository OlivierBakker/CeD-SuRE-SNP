package nl.umcg.suresnp.pipeline.io.bedreader;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.FeatureReader;
import htsjdk.tribble.TabixFeatureReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BlockCompressedIpcrFileReader;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;
import org.broad.igv.bbfile.BBFileHeader;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BedFeature;
import org.broad.igv.bbfile.BigBedIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

// TODO: could stand to cleanup the duplicate code with BlockCompressedGenomicAnnotationReader a bit by making a super
public class BigBedGenomicAnnotationReader implements GenomicAnnotationProvider {

    private static final Logger LOGGER = Logger.getLogger(BigBedGenomicAnnotationReader.class);
    private String[] header;

    private BBFileReader featureReader;
    private List<Locatable> intervals;
    private int index;
    private BigBedIterator iteratorCache;
    private boolean appendChrToContig;

    public BigBedGenomicAnnotationReader(GenericFile inputFile, List<Locatable> intervals, boolean appendChrToContig) throws IOException {
        featureReader = new BBFileReader(inputFile.getPathAsString());

        // Intervals to loop over
        this.intervals = intervals;
        this.index = 0;
        this.appendChrToContig = appendChrToContig;

        if (appendChrToContig) {
            LOGGER.info("Appending chr to contig names for lookup only");
        } else {
            LOGGER.info("NOT appending chr to contig names for lookup only");
        }

        this.header = new String[featureReader.getBBFileHeader().getFieldCount()];
        for (int i=0; i < this.header.length; i++) {
            header[i] = inputFile.getBaseName() + "_" + i;
        }
        LOGGER.info("Detected " + header.length + " fields");

        // Reset the iterator
        this.index = 0;
        this.iteratorCache = null;
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
            if (iteratorCache != null && !iteratorCache.hasNext()) {
                iteratorCache = null;
            }

            if (iteratorCache == null) {
                iteratorCache = query(intervals.get(index));
                index++;
            }

            if (iteratorCache.hasNext()) {
                BedFeature tmp = iteratorCache.next();
                String chr = tmp.getChromosome().replaceFirst("chr", "");

                return new GenericGenomicAnnotationRecord(chr, tmp.getStartBase(), tmp.getEndBase(), tmp.getRestOfFields());
            }
        }
        return null;

    }

    @Override
    public void close() throws IOException {
        featureReader.close();
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

    public BigBedIterator query(Locatable interval) {
        if (appendChrToContig) {
            return featureReader.getBigBedIterator( "chr" + interval.getContig(), interval.getStart(), "chr" + interval.getContig(), interval.getEnd(), false);
        } else {
            return featureReader.getBigBedIterator( interval.getContig(), interval.getStart(), interval.getContig(), interval.getEnd(), false);
        }
    }

}
