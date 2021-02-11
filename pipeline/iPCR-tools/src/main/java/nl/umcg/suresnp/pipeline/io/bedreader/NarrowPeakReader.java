package nl.umcg.suresnp.pipeline.io.bedreader;

import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.filters.NarrowPeakFilter;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class NarrowPeakReader implements BedRecordProvider, Iterable<NarrowPeakRecord>, Iterator<NarrowPeakRecord> {

    private static final Logger LOGGER = Logger.getLogger(NarrowPeakReader.class);
    private BufferedReader reader;
    private static String sep = "\t";
    private String pattern;
    private boolean trimChrFromContig;

    public NarrowPeakReader(GenericFile inputFile) throws IOException {
        this(inputFile, null, false);
    }

    public NarrowPeakReader(GenericFile inputFile, String pattern) throws IOException {
        this(inputFile, pattern, false);
    }

    public NarrowPeakReader(GenericFile inputFile, String pattern, boolean trimChrFromContig) throws IOException {
        this.reader = inputFile.getAsBufferedReader();
        this.pattern = pattern;
        this.trimChrFromContig = trimChrFromContig;
    }

    public NarrowPeakReader(GenericFile inputFile, boolean trimChrFromContig) throws IOException {
        this(inputFile, null, false);
    }


    @Override
    public List<BedRecord> getBedRecordAsList() throws IOException {
        List<BedRecord> output = new TreeList<>();
        NarrowPeakRecord curRecord = getNextRecord();
        int i = 0;

        while (curRecord != null) {
            logProgress(i, 1000000, "NarrowPeakReader");
            i++;
            output.add(new BedRecord(curRecord));
            curRecord = getNextRecord();
        }
        LOGGER.info("Read " + i + " records");
        return output;
    }

    public List<NarrowPeakRecord> getNarrowPeakRecordsAsList() throws IOException {
        List<NarrowPeakRecord> output = new TreeList<>();
        NarrowPeakRecord curRecord = getNextRecord();
        int i = 0;

        while (curRecord != null) {
            logProgress(i, 1000000, "NarrowPeakReader");
            i++;
            output.add(curRecord);
            curRecord = getNextRecord();
        }
        LOGGER.info("Read " + i + " records");
        return output;
    }

    public IntervalTreeMap<NarrowPeakRecord> getNarrowPeakRecordsAsTreeMap() throws IOException {
        return getNarrowPeakRecordsAsTreeMap(new ArrayList<>());
    }

    public IntervalTreeMap<NarrowPeakRecord> getNarrowPeakRecordsAsTreeMap(Collection<NarrowPeakFilter> filters) throws IOException {
        IntervalTreeMap<NarrowPeakRecord> output = new IntervalTreeMap<>();
        NarrowPeakRecord curRecord = getNextRecord();
        int i = 0;

        while (curRecord != null) {
            logProgress(i, 1000000, "NarrowPeakReader");
            i++;
            boolean passes = true;
            for (NarrowPeakFilter curFilter : filters) {
                if (!curFilter.passesFilter(curRecord)) {
                    passes = false;
                    break;
                }
            }

            if (passes) {
                output.put(curRecord, curRecord);
            }
            curRecord = getNextRecord();
        }
        LOGGER.info("Read " + i + " records");
        return output;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public Iterator<NarrowPeakRecord> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        try {
            return reader.ready();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public NarrowPeakRecord next() {
        if (hasNext()) {
            try {
                return getNextRecord();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    protected static NarrowPeakRecord parseNarrowPeakRecord(String line) {
        return parseNarrowPeakRecord(line, false);
    }

    protected static NarrowPeakRecord parseNarrowPeakRecord(String line, boolean trimChrFromContig) {
        String[] content = line.split(sep);
        String contig = content[0];
        if (trimChrFromContig) {
            contig = contig.replaceFirst("chr", "");
        }

        return new NarrowPeakRecord(contig,
                Integer.parseInt(content[1]),
                Integer.parseInt(content[2]),
                content[3],
                Double.parseDouble(content[4]),
                content[5].charAt(0),
                Double.parseDouble(content[6]),
                Double.parseDouble(content[7]),
                Double.parseDouble(content[8]),
                Integer.parseInt(content[9]));
    }

    private NarrowPeakRecord getNextRecord() throws IOException {

        String line = reader.readLine();
        if (line != null) {
            NarrowPeakRecord record = parseNarrowPeakRecord(line, trimChrFromContig);
            if (pattern != null) {
                record.setName(record.getName().replace(pattern, ""));
            }
            return record;
        } else {
            return null;
        }
    }

}
