package nl.umcg.suresnp.pipeline.records.ipcrrecord.filters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InRegionFilter implements IpcrRecordFilter, Iterator<BedRecord> {

    private static final IpcrRecordFilterType type = IpcrRecordFilterType.IN_REGION;
    private boolean filterFailed;
    private int currentIndex;
    private List<String> contigs;
    private List<Integer> starts;
    private List<Integer> stops;

    public InRegionFilter() {
        this.contigs = new ArrayList<>();
        this.starts = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.currentIndex = 0;
        this.filterFailed = false;
    }

    public InRegionFilter(List<String> contigs, List<Integer> starts, List<Integer> stops) {
        this.contigs = contigs;
        this.starts = starts;
        this.stops = stops;
        this.currentIndex = 0;
        this.filterFailed = false;
    }

    public InRegionFilter(String contig, int start, int stop) {
        this.contigs = new ArrayList<>();
        this.starts = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.currentIndex = 0;
        this.filterFailed = false;
        addRegion(contig, start, stop);
    }


    public InRegionFilter(GenericFile regionFile) throws IOException, ParseException {
        this.contigs = new ArrayList<>();
        this.starts = new ArrayList<>();
        this.stops = new ArrayList<>();
        this.currentIndex = 0;
        this.filterFailed = false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(regionFile.getAsInputStream()));
        String line = reader.readLine();

        while (line != null) {
            String[] record = line.split("\t");

            if (record.length == 3) {
                contigs.add(record[0]);
                starts.add(Integer.parseInt(record[1]));
                stops.add(Integer.parseInt(record[2]));
            } else {
                throw new ParseException("Not a valid 3 collumn, tab seperarted, bed recod", 1);
            }
            line = reader.readLine();
        }

        reader.close();
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
        currentIndex++;

        if (cachedIndex < contigs.size()) {
            return new BedRecord(contigs.get(cachedIndex), starts.get(cachedIndex), stops.get(cachedIndex));
        } else {
            return null;
        }
    }
}
