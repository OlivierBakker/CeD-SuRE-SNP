package nl.umcg.suresnp.pipeline.utils;

import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GenomicRegionPileup  {

    private static final Logger LOGGER = Logger.getLogger(GenomicRegionPileup.class);
    private String fieldToPileup;
    private List<BedRecord> records;
    private int binWidth;

    private int index;

    public GenomicRegionPileup(int binWidth) {
        this.records = new ArrayList<>();
        this.binWidth = binWidth;
    }

    public GenomicRegionPileup(int binWidth, String fieldToPileup) {
        this.records = new ArrayList<>();
        this.binWidth = binWidth;
        this.fieldToPileup = fieldToPileup;
    }

    public void addIpcrRecord(IpcrRecord record) {

        // Assumes records are provided sorted
        if (index == 0) {
            createNewBedRecord(record);
            index ++;
            return;
        }

        BedRecord previousRecord = records.get(records.size()-1);
        if (!previousRecord.contigsMatch(previousRecord)) {
            createNewBedRecord(record);
        } else if (!previousRecord.overlaps(record)) {

/*
            if (previousRecord.getEnd() > record.getEnd()) {
                LOGGER.error("Current end " + record.getEnd() + " > previous: " + previousRecord.getEnd());
                LOGGER.error("Input is likely not sorted on contig and position");
                throw new IllegalArgumentException("Input is likely not sorted on contig and position");
            }
*/

            BedRecord linkingRec = new BedRecord(record.getContig(), previousRecord.getEnd(), record.getStart());
            records.add(linkingRec);

            createNewBedRecord(record);
        } else {

/*            if (previousRecord.getEnd() > record.getEnd()) {
                LOGGER.error("Current end " + record.getEnd() + " > previous: " + previousRecord.getEnd());
                LOGGER.error("Input is likely not sorted on contig and position");
                throw new IllegalArgumentException("Input is likely not sorted on contig and position");
            }*/

            updatePreviousBedRecord(record);

        }

        index ++;
    }

    public List<BedRecord> getPileup() {
        return records;
    }

    private void updatePreviousBedRecord(IpcrRecord record) {

        double curScore = records.get(records.size()-1).getScore();

        if (fieldToPileup != null) {
            if (fieldToPileup.equals("IPCR")) {
                records.get(records.size()-1).setScore(curScore + record.getIpcrDuplicateCount());
            } else {
                records.get(records.size()-1).setScore(curScore + record.getBarcodeCountPerSample().get(fieldToPileup));
            }
        } else {
            records.get(records.size()-1).setScore(curScore + 1);
        }
    }


    private void createNewBedRecord(IpcrRecord record) {
        BedRecord newRec = new BedRecord(record.getContig(), record.getOrientationIndependentStart(), record.getOrientationIndependentStart() + binWidth);

        if (fieldToPileup != null) {
            if (fieldToPileup.equals("IPCR")) {
                newRec.setScore(record.getIpcrDuplicateCount());
            } else {
                newRec.setScore(record.getBarcodeCountPerSample().get(fieldToPileup));
            }
        } else {
            newRec.setScore(1);
        }

        records.add(newRec);
    }
}
