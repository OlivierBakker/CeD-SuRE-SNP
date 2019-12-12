package nl.umcg.suresnp.pipeline.ipcrrecords.filters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class InRegionFilter implements IpcrRecordFilter {

    private List<String> contigs;
    private List<Integer> starts;
    private List<Integer> stops;

    public InRegionFilter() {
        this.contigs = new ArrayList<>();
        this.starts = new ArrayList<>();
        this.stops = new ArrayList<>();
    }

    public InRegionFilter(List<String> contigs, List<Integer> starts, List<Integer> stops) {
        this.contigs = contigs;
        this.starts = starts;
        this.stops = stops;
    }

    public InRegionFilter(GenericFile regionFile) throws IOException, ParseException {
        this.contigs = new ArrayList<>();
        this.starts = new ArrayList<>();
        this.stops = new ArrayList<>();

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
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    @Override
    public String getFilterName() {
        return "InRegionFilter";
    }
}
