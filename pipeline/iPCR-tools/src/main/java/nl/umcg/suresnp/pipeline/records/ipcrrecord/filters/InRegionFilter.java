package nl.umcg.suresnp.pipeline.records.ipcrrecord.filters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class InRegionFilter implements IpcrRecordFilter, Iterator<BedRecord>, Iterable<BedRecord> {

    private static final Logger LOGGER = Logger.getLogger(InRegionFilter.class);
    private static final IpcrRecordFilterType type = IpcrRecordFilterType.IN_REGION;
    private boolean filterFailed;
    private int currentIndex;
    private List<String> contigs;
    private List<Integer> starts;
    private List<Integer> stops;

    public InRegionFilter(String contig, int start, int stop) {
        this.contigs = new ArrayList<>();
        this.starts = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.currentIndex = 0;
        this.filterFailed = false;
        addRegion(contig, start, stop);
    }

    public InRegionFilter(GenericFile regionFile, boolean skipFirstLine) throws IOException, ParseException {
        this.contigs = new ArrayList<>();
        this.starts = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.currentIndex = 0;
        this.filterFailed = false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(regionFile.getAsInputStream()));
        String line = reader.readLine();

        if (line.split("\t").length > 3) {
            LOGGER.warn("More that 3 columns detected, assuming the first 3 are in BED format");
        }

        if (skipFirstLine) {
            line = reader.readLine();
        }

        while (line != null) {
            String[] record = line.split("\t");
            if (record.length >= 3) {
                contigs.add(record[0]);
                starts.add(Integer.parseInt(record[1]));
                stops.add(Integer.parseInt(record[2]));
            } else {
                throw new ParseException("Not a valid 3 collumn, tab seperarted, bed recod", 1);
            }
            line = reader.readLine();
        }

        reader.close();

        LOGGER.info("Instantiated region filter for " + contigs.size() + " loci");
    }

    public InRegionFilter(Collection<BedRecord> records) throws IOException, ParseException {
        this.contigs = new ArrayList<>();
        this.starts = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.currentIndex = 0;
        this.filterFailed = false;

        for (BedRecord record: records) {
            addRegion(record);
        }

    }

    public void addRegion(BedRecord record) {
        contigs.add(record.getContig());
        starts.add(record.getStart());
        stops.add(record.getEnd());
    }

    public void addRegion(String contig, int start, int stop) {
        contigs.add(contig);
        starts.add(start);
        stops.add(stop);
    }

    @Override
    public boolean passesFilter(IpcrRecord ipcrRecord) {
        int i = 0;
        for (String contig : contigs) {
            if (ipcrRecord.getContig().equals(contig)) {
                if (ipcrRecord.isFullyInsideWindow(starts.get(i), stops.get(i))) {
                    return !filterFailed;
                }
            }
            i++;
        }
        return filterFailed;
    }

    @Override
    public void invertFilter() {
        filterFailed = !filterFailed;
    }

    @Override
    public String getFilterName() {
        return type.toString();
    }

    @Override
    public IpcrRecordFilterType getFilterType() {
        return type;
    }

    public List<String> getContigs() {
        return contigs;
    }

    public List<Integer> getStarts() {
        return starts;
    }

    public List<Integer> getStops() {
        return stops;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < contigs.size();
    }

    @Override
    public BedRecord next() {
        int cachedIndex = currentIndex;

        if (hasNext()) {
            currentIndex++;
            return new BedRecord(contigs.get(cachedIndex), starts.get(cachedIndex), stops.get(cachedIndex));
        } else {
            // reset the index if it is the exit condition
            //currentIndex = 0;
            return null;
        }

/*        int cachedIndex = currentIndex;
        currentIndex++;

        if (cachedIndex < contigs.size()) {
            return new BedRecord(contigs.get(cachedIndex), starts.get(cachedIndex), stops.get(cachedIndex));
        } else {
            return null;
        }*/
    }

    @Override
    public Iterator<BedRecord> iterator() {
        return this;
    }

    public void resetIndex() {
        currentIndex = 0;
    }
}
