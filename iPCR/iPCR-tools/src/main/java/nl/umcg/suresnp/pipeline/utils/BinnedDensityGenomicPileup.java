package nl.umcg.suresnp.pipeline.utils;

import nl.umcg.suresnp.pipeline.IpcrTools;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class BinnedDensityGenomicPileup implements GenomicRegionPileup {

    private static final Logger LOGGER = Logger.getLogger(BinnedDensityGenomicPileup.class);

    private String fieldToPileup;
    private int binWidth;
    private Map<String, List<BedRecord>> records;

    public BinnedDensityGenomicPileup(int binWidth, String fieldToPileup) {
        this.fieldToPileup = fieldToPileup;
        this.binWidth = binWidth;
        this.records = new HashMap<>();

        for (String chr : B37GenomeInfo.getChromosomesNoChr()) {
            records.put(chr, makeBedRecords(binWidth, B37GenomeInfo.getChromSize(chr), chr));
        }
    }

    private static List<BedRecord> makeBedRecords(int binWidth, int size, String contig) {

        List<BedRecord> output = new ArrayList<>(size / binWidth);
        int curStart = 0;
        int idx = 0;

        LOGGER.info("Making genomic intervals for contig: " + contig);
        while (curStart + binWidth < size) {
            logProgress(idx, 1000000, "BinnedDensityGenomicPileup");
            output.add(new BedRecord(contig, curStart, curStart + binWidth));

            curStart = curStart + binWidth;
            idx++;
        }

        LOGGER.info("Done, made " + idx + " intervals with binsize: " + binWidth);

        return output;
    }

    @Override
    public void addIpcrRecord(IpcrRecord record) {
        List<BedRecord> overlappingRegions = queryInterval(record.getContig(), record.getOrientationIndependentStart(), record.getOrientationIndependentEnd());
        for (BedRecord curRec : overlappingRegions) {
            if (fieldToPileup.equals("IPCR")) {
                curRec.setScore(curRec.getScore() + record.getIpcrDuplicateCount());
            } else {
                curRec.setScore(curRec.getScore() + record.getBarcodeCountPerSample().get(fieldToPileup));
            }
        }
    }

    @Override
    public List<BedRecord> getPileup() {
        return collapseZeroCountRecords();
    }

    @Override
    public BedRecord getNextRecord() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    private List<BedRecord> collapseZeroCountRecords() {

        List<BedRecord> collapsedRecords = new ArrayList<>();
        for (Map.Entry<String, List<BedRecord>> entry : records.entrySet()) {
            BedRecord prevRec = null;

            for (BedRecord curRec : entry.getValue()) {
                // Catches first iter
                if (prevRec == null) {
                    prevRec = curRec;
                    continue;
                }

                // If current and previous recs zero merge them
                if (curRec.getScore() == 0 && prevRec.getScore() == 0) {
                    prevRec.setEnd(curRec.getEnd());
                } else  if (curRec.getScore() == 0  && prevRec.getScore() != 0) {
                    collapsedRecords.add(prevRec);
                    prevRec = curRec;
                } else if (curRec.getScore() != 0) {
                    // If the current record != 0
                    collapsedRecords.add(prevRec);
                    //collapsedRecords.add(curRec);
                    prevRec = curRec;
                }

            }
            //entry.setValue(collapsedRecords);
            //outputList.addAll(collapsedRecords);
        }
        // after this the records are no longer valid, so set everthing to null
        records = null;
        return collapsedRecords;
    }

    private List<BedRecord> queryInterval(String contig, int start, int end) {

        if (start == 0) {
            start = 1;
        }

        if (!records.containsKey(contig.toUpperCase())) {
            return new ArrayList<>();
        }
        List<BedRecord> curContig = records.get(contig.toUpperCase());

        if ((start / binWidth) - 1 > curContig.size()) {
            throw new IllegalArgumentException("Start index is larger then contig");
        }

        if ((end / binWidth) - 1 > curContig.size()) {
            throw new IllegalArgumentException("End index is larger then contig");
        }

        final List<BedRecord> subset = curContig.subList((start / binWidth) - 1, (end / binWidth) - 1);
        return subset;
    }

}
