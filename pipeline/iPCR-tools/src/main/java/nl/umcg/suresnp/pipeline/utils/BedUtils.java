package nl.umcg.suresnp.pipeline.utils;

import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class BedUtils {

    private static final Logger LOGGER = Logger.getLogger(BedUtils.class);

    public static List<BedRecord>[] intersectSortedBedRecords(List<BedRecord> setOne, List<BedRecord> setTwo) throws Exception {

        // TODO: Refactor this so it is on a per contig basis to account for missing contigs, current impl can only handle one missing gap
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
        String curContig = curRecOne.getContig();

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
                if (i % 100 == 0) {
                    LOGGER.debug("iterOne: " + iterOne + " iterTwo: " + iterTwo + " i: " + i + " curChr: " + curContig);
                }
                if (curRecOne.overlaps(curRecTwo)) {
                    outOne.add(curRecOne);
                    outTwo.add(curRecTwo);
                    iterOne ++;
                    iterTwo ++;
                    curRecOne = setOne.get(iterOne);
                    curRecTwo = setTwo.get(iterTwo);
                } else if (curRecOne.getEnd() < curRecTwo.getStart()) {
                    iterOne ++;
                    curRecOne = setOne.get(iterOne);
                } else if (curRecOne.getStart() > curRecTwo.getEnd()){
                    iterTwo ++;
                    curRecTwo = setTwo.get(iterTwo);
                }
                curContig = curRecOne.getContig();
            } else {

                if (i % 100 == 0) {
                    LOGGER.debug("Cur chr:" + curContig +  " one: " + curRecOne.getContig() + " two: " + curRecTwo.getContig() + " i: " +i);
                }

                // This block deals with missing contigs
                // Case where the first contigs are missing in either file
                if (iterOne < 1) {
                    iterOne ++;
                    curRecOne = setOne.get(iterOne);
                } else if (iterTwo < 1) {
                    iterTwo ++;
                    curRecTwo = setTwo.get(iterTwo);
                }

                // Figure out to advance iterOne or iterTwo
                if (curRecOne.getContig().equals(setOne.get(iterOne - 1).getContig()) ) {
                    iterOne ++;
                    curRecOne = setOne.get(iterOne);
                    curContig = curRecTwo.getContig();
                } else if (curRecTwo.getContig().equals(setTwo.get(iterTwo - 1).getContig())) {
                    iterTwo ++;
                    curRecTwo = setTwo.get(iterTwo);
                    curContig = curRecOne.getContig();
                } else  if (!curContig.equals(curRecOne.getContig()) && curContig.equals(curRecTwo.getContig())) {
                    iterOne ++;
                    curRecOne = setOne.get(iterOne);
                } else if (!curContig.equals(curRecTwo.getContig()) && curContig.equals(curRecOne.getContig())) {
                    iterTwo ++;
                    curRecTwo = setTwo.get(iterTwo);
                }

            }

            if(i > (setOne.size() + setTwo.size())) {
                LOGGER.info("RecOne-1: " + setOne.get(iterOne - 1).toBedString());
                LOGGER.info("RecOne: " + curRecOne.toBedString() + " iter: " + iterOne);
                LOGGER.info("RecOne+1: " + setOne.get(iterOne + 1).toBedString());

                LOGGER.info("RecTwo-1: " + setTwo.get(iterTwo - 1).toBedString());
                LOGGER.info("RecTwo: " + curRecTwo.toBedString() + " iter: " + iterTwo);
                LOGGER.info("RecTwo+1: " + setTwo.get(iterTwo + 1).toBedString());

                throw new Exception("It seems exit conditions were not met. Are you sure both inputs were sorted?");
            }
            i++;
        }

        LOGGER.info("Overlapped " + outOne.size() + ", " + outTwo.size() + " records");

        return new List[]{outOne, outTwo};
    }



}
