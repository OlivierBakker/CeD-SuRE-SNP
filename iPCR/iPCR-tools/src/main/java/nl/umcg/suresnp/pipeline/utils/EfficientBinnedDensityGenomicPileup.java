package nl.umcg.suresnp.pipeline.utils;

import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Make this conform to iterable
public class EfficientBinnedDensityGenomicPileup implements GenomicRegionPileup {

    private static final Logger LOGGER = Logger.getLogger(EfficientBinnedDensityGenomicPileup.class);

    private AdaptableScoreProvider scoreProvider;
    private int binWidth;
    private Map<String, int[]> records;

    private BedRecord prevRec;
    private int index;
    private int conIndex;
    private String[] contigs;

    public EfficientBinnedDensityGenomicPileup(int binWidth, AdaptableScoreProvider scoreProvider) {
        this.scoreProvider = scoreProvider;
        this.binWidth = binWidth;
        this.records = new HashMap<>();
        this.conIndex = 0;
        this.contigs = new String[B37GenomeInfo.getChromosomesNoChr().size()];

        for (String chr : B37GenomeInfo.getChromosomesNoChr()) {
            this.records.put(chr, new int[B37GenomeInfo.getChromSize(chr) / binWidth]);
            this.contigs[this.conIndex] = chr;
            this.conIndex++;
        }
        this.conIndex = 0;
        LOGGER.debug("Making pileup over " + contigs.length + " contigs");
    }

    @Override
    public void addIpcrRecord(IpcrRecord record) {

        String contig = record.getContig();
        if (records.containsKey(contig)) {
            // 1 indexed
            int start = record.getOrientationIndependentStart();
            int end = record.getOrientationIndependentEnd();

            if (start == 0) {
                start = 1;
            }

            // Indices in array
            int indexStart = (start / binWidth) - 1;
            int indexStop = (end / binWidth) - 1;

            if (indexStart > records.get(contig).length) {
                throw new IndexOutOfBoundsException("Start index is larger then contig");
            }

            if (indexStop > records.get(contig).length) {
                throw new IndexOutOfBoundsException("End index is larger then contig");
            }

            for (int curRec = indexStart; curRec <= indexStop; curRec++) {
                records.get(contig)[curRec] = records.get(contig)[curRec] + (int) scoreProvider.getScore(record);
            }
        }
    }

    @Override
    public List<BedRecord> getPileup() {
        throw new IllegalArgumentException("NotImplemented");
    }

    @Override
    public BedRecord getNextRecord() {

        if (conIndex < contigs.length) {
            if (this.hasNext(contigs[conIndex])) {
                return this.getNextRecord(contigs[conIndex]);
            } else {
                index = 0;
                conIndex++;
                if (conIndex < contigs.length) {
                    LOGGER.debug("Next contig: " + contigs[conIndex]);
                    return this.getNextRecord();
                }
            }
        }

        return null;
    }

    @Override
    public boolean hasNext() {
        if (conIndex < (contigs.length - 1)) {
            return true;
        } else if (conIndex < contigs.length) {
            return hasNext(contigs[conIndex]);
        } else {
            return false;
        }
    }

    public boolean hasNext(String contig) {
        return index < (B37GenomeInfo.getChromSize(contig) / binWidth) - 1;
    }

    private BedRecord getNextRecord(String contig) {
        int[] scores = records.get(contig);

        BedRecord curRec;

        if (index == 0) {
            curRec = new BedRecord(contig, 1, 1 + binWidth, scores[index]);
            prevRec = curRec;
        } else {
            curRec = new BedRecord(contig, prevRec.getEnd(), prevRec.getEnd() + binWidth, scores[index]);
        }

        if (curRec.getScore() != 0) {
            prevRec = curRec;
            index++;
            return curRec;
        } else {
            // Iterate until curRec.score != 0
            prevRec.setStart(prevRec.getEnd());
            prevRec.setScore(0);
            while (curRec.getScore() == 0) {
                prevRec.setEnd(curRec.getEnd());
                index++;
                if (hasNext(contig)) {
                    curRec = new BedRecord(contig, prevRec.getEnd(), prevRec.getEnd() + binWidth, scores[index]);
                } else {
                    break;
                }
            }

            return prevRec;
        }


    }































 /*   // Could do this using recursion, but the stacksize will not be big enough, so just returning null
    private BedRecord getNextRecord(String contig) {
        int[] scores = records.get(contig);

        if (hasNext(contig)) {
            if (index == 0) {
                prevRec = new BedRecord(contig, index, index + binWidth, scores[index]);
                index++;
                if (prevRec.getScore() > 0) {
                    return prevRec;
                } else {
                    return null;
                }
            }

            BedRecord cache;
            BedRecord curRec = new BedRecord(contig, prevRec.getEnd(), prevRec.getEnd() + binWidth, scores[index]);

            // If both zero, merge and advance
            if (curRec.getScore() == 0 && prevRec.getScore() == 0) {
                curRec.setStart(prevRec.getStart());
                index++;
                return null;
            } else  if (curRec.getScore() == 0  && prevRec.getScore() != 0) {


            } else if (curRec.getScore() != 0) {


            }

            if (curRec.getScore() != 0) {
                cache = prevRec;
                prevRec = curRec;
                index++;
                return cache;
            } else {
                prevRec = curRec;
                index++;
                return null;
            }
        }

        return null;
    }
*/
}
