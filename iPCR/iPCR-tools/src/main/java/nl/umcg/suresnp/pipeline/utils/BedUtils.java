package nl.umcg.suresnp.pipeline.utils;

import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class BedUtils {

    private static final Logger LOGGER = Logger.getLogger(BedUtils.class);

    public static List<BedRecord>[] intersectSortedBedRecords(List<? extends BedRecord> setOne, List<? extends BedRecord> setTwo) {

        // TODO: Cleanup this code a bit, make sure the last records are properly accounted for
        LOGGER.info("Input A: " + setOne.size());
        LOGGER.info("Input B: " + setTwo.size());

        List<BedRecord> outOne = new ArrayList<>();
        List<BedRecord> outTwo = new ArrayList<>();

        // Make something with 2 stacks. As they are sorted. If recA < recB advance recA if recA > recB advance recA
        // Check every loop for equality. If so keep the record in output sets

        int i=0;
        int iterOne = 0;
        int iterTwo = 0;
        boolean run = true;
        BedRecord curRecOne = setOne.get(iterOne);
        BedRecord curRecTwo = setTwo.get(iterTwo);

        iterOne ++;
        iterTwo ++;

        while (run) {
            logProgress(i, 1000000, "BedUtils");

            // Exit condition
            // -3 since starts at one
            if (curRecOne == null || curRecTwo == null || iterOne > setOne.size() -3 || iterTwo > setTwo.size() -3) {
                run = false;
            }

            // If the same chromosome
            if (curRecOne.getContig().equals(curRecTwo.getContig())) {
                LOGGER.debug("iterOne: " + iterOne + " iterTwo: " + iterTwo + " i: " +i);
                if (curRecOne.overlaps(curRecTwo)) {
                    outOne.add(curRecOne);
                    outTwo.add(curRecTwo);
                    iterOne ++;
                    iterTwo ++;
                    curRecOne = setOne.get(iterOne);
                    curRecTwo = setTwo.get(iterTwo);
                } else if (curRecOne.getStop() < curRecTwo.getStart()) {
                    iterOne ++;
                    curRecOne = setOne.get(iterOne);
                } else if (curRecOne.getStart() > curRecTwo.getStop()){
                    iterTwo ++;
                    curRecTwo = setTwo.get(iterTwo);
                }
            } else {
                // Figure out to advance iterOne or iterTwo
                if (curRecOne.getContig().equals(setOne.get(iterOne - 1).getContig())) {
                    iterOne ++;
                    curRecOne = setOne.get(iterOne);
                } else if (curRecTwo.getContig().equals(setTwo.get(iterTwo - 1).getContig())) {
                    iterTwo ++;
                    curRecTwo = setTwo.get(iterTwo);
                }
            }
            i++;
        }

        // Clean last pair

        LOGGER.info("Overlapped " + outOne.size() + ", " + outTwo.size() + " records");

        return new List[]{outOne, outTwo};
    }



}
