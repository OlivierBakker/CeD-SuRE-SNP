package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.BedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.DiscardedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.ipcrrecords.BasicIpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;
import sun.net.www.content.text.Generic;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CollapseIpcr {

    private static final Logger LOGGER = Logger.getLogger(CollapseIpcr.class);
    private final CollapseIpcrParameters params;
    private IpcrOutputWriter outputWriter;
    private IpcrOutputWriter discardedOutputWriter;

    public CollapseIpcr(CollapseIpcrParameters params) throws IOException {
        this.params = params;
        this.outputWriter = params.getOutputWriter();
        this.discardedOutputWriter = new DiscardedIpcrRecordWriter(new File(params.getOutputPrefix() + ".collapsed.discarded.reads.txt"), false);
    }

    public void run() throws IOException {

        try {
            IpcrRecordProvider provider = new IpcrFileReader(new GenericFile(params.getInputFile()), true);
            List<IpcrRecord> records = provider.getRecordsAsList();
            provider.close();

            LOGGER.info("Started sorting " + records.size() + " reads");
            long start = System.currentTimeMillis();
            records.sort(Comparator
                    .comparing(IpcrRecord::getBarcode)
                    .thenComparing(IpcrRecord::getPrimaryReadName));
            long stop = System.currentTimeMillis();
            LOGGER.info("Done sorting. Took: " + ((stop - start) / 1000) + " seconds");

            // Collapse iPCR records
            List<IpcrRecord> outputList = new TreeList<>();
            ArrayList<IpcrRecord> duplicateRecordCache = new ArrayList<>();

            int j = 0;
            String cachedBarcode = "";
            for (IpcrRecord record : records) {
                // Case for first record
                if (j == 0) {
                    cachedBarcode = record.getBarcode();
                    duplicateRecordCache.add(record);
                    j++;
                    continue;
                }

                if (record.getBarcode().equals(cachedBarcode)) {
                    duplicateRecordCache.add(record);
                } else {

                    if (duplicateRecordCache.size() == 1) {
                        IpcrRecord tmp = duplicateRecordCache.get(0);
                        tmp.setIpcrDuplicateCount(1);
                        outputList.add(tmp);
                    } else if (duplicateRecordCache.size() > 1) {
                        // Collapse previous records;
                        outputList.add(createConsensusRecord(duplicateRecordCache));
                    }

                    // Set new record
                    duplicateRecordCache.clear();
                    cachedBarcode = record.getBarcode();
                    duplicateRecordCache.add(record);
                }

                j++;
            }
            // Proccess the last records remaining in the DuplicateRecordCache
            if (duplicateRecordCache.size() == 1) {
                IpcrRecord tmp = duplicateRecordCache.get(0);
                tmp.setIpcrDuplicateCount(1);
                outputList.add(tmp);
            } else if (duplicateRecordCache.size() > 1) {
                // Collapse previous records;
                outputList.add(createConsensusRecord(duplicateRecordCache));
            }

            LOGGER.info("Done, collapsed " + outputList.size() + " valid records");

            // Sort on position
            LOGGER.info("Started sorting " + outputList.size() + " records");
            start = System.currentTimeMillis();
            outputList.sort(Comparator
                    .comparing(IpcrRecord::getContig)
                    .thenComparing(IpcrRecord::getOrientationIndependentStart));
            stop = System.currentTimeMillis();
            LOGGER.info("Done sorting. Took: " + ((stop - start) / 1000) + " seconds");

            // Write the output
            IpcrOutputWriter outputWriter1 = new BedIpcrRecordWriter(new File(params.getOutputPrefix()), false);
            outputWriter1.setBarcodeCountFilesSampleNames(provider.getSamples());
            outputWriter1.writeHeader();

            for (IpcrRecord curRecord : outputList) {
                outputWriter1.writeRecord(curRecord);
            }
            LOGGER.info("Done, writing pileup of size " + outputList.size());
            outputWriter1.flushAndClose();


            //TODO: Hardcoded sample name
            List<IpcrRecord> pileup = makePileup(outputList,provider.getSamples()[0], 250);

            // Write the output
            outputWriter.setBarcodeCountFilesSampleNames(provider.getSamples());
            outputWriter.writeHeader();

            for (IpcrRecord record : pileup) {
                outputWriter.writeRecord(record);
            }
            LOGGER.info("Done, writing pileup of size " + pileup.size());


            outputWriter.flushAndClose();
            discardedOutputWriter.flushAndClose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;

    }

    private List<IpcrRecord> makePileup(List<IpcrRecord> records, String sample, int fragmentSize) {
        // assumes records are sorted on position
        // TODO: does not handle the ends of contigs properly, fix this
        List<IpcrRecord> outputPileup = new TreeList<>();
        List<IpcrRecord> pileupCache = new ArrayList<>();

        String cachedContig = records.get(0).getContig();
        int contigStop = CollapseIpcrParameters.getChromSize(cachedContig);
        int windowStart = 0;
        int windowEnd = fragmentSize;
        IpcrRecord tmpRecord;
        Map<String, Integer> cDnaCounts;

        for (IpcrRecord curRecord : records) {

            if (curRecord.getContig().equals(cachedContig)) {
                if (curRecord.isStartInWindow(windowStart, windowEnd)) {
                    pileupCache.add(curRecord);
                } else {
                    tmpRecord = new BasicIpcrRecord(cachedContig, windowStart, windowEnd);
                    tmpRecord.setIpcrDuplicateCount(pileupCache.size());
                    cDnaCounts = new HashMap<>();

                    int cDnaCount = 0;
                    if (pileupCache.size() > 0) {
                        for (IpcrRecord rec : pileupCache) {
                            cDnaCount += rec.getBarcodeCountPerSample().get(sample);
                        }
                    }
                    cDnaCounts.put(sample, cDnaCount);
                    tmpRecord.setBarcodeCountPerSample(cDnaCounts);
                    outputPileup.add(tmpRecord);
                    pileupCache.clear();

                    // Now proccess the current record
                    windowStart = windowEnd;
                    windowEnd = windowEnd + fragmentSize;

                    if (curRecord.isStartInWindow(windowStart, windowEnd)) {
                        pileupCache.add(curRecord);
                    } else {
                        tmpRecord = new BasicIpcrRecord(cachedContig, windowStart, curRecord.getOrientationIndependentStart());
                        tmpRecord.setIpcrDuplicateCount(0);
                        cDnaCounts = new HashMap<>();
                        cDnaCounts.put(sample, 0);
                        tmpRecord.setBarcodeCountPerSample(cDnaCounts);
                        outputPileup.add(tmpRecord);
                        pileupCache.clear();

                        windowStart = curRecord.getOrientationIndependentStart();
                        windowEnd = windowStart + fragmentSize;
                    }
                }
            } else {
                pileupCache.clear();
                cachedContig = curRecord.getContig();
                windowStart = 0;
                windowEnd = fragmentSize;

                if (curRecord.isStartInWindow(windowStart, windowEnd)) {
                    pileupCache.add(curRecord);
                } else {
                    tmpRecord = new BasicIpcrRecord(cachedContig, windowStart, curRecord.getOrientationIndependentStart());
                    tmpRecord.setIpcrDuplicateCount(0);
                    cDnaCounts = new HashMap<>();
                    cDnaCounts.put(sample, 0);
                    tmpRecord.setBarcodeCountPerSample(cDnaCounts);
                    outputPileup.add(tmpRecord);
                    pileupCache.clear();

                    windowStart = curRecord.getOrientationIndependentStart();
                    windowEnd = windowStart + fragmentSize;
                }
            }

        }

        LOGGER.info("Made pileup of " + outputPileup.size() + " fragments");
        return outputPileup;
    }


    private IpcrRecord createConsensusRecord(List<IpcrRecord> records) throws IOException {

        // Distance in bp to consider the same alignment
        int maxDistance = 100;

        IpcrRecord consensusRecord = null;
        int consensusIndex = 0;
        int i = 0;

        // Find the record with the best mapping quality for both reads
        for (IpcrRecord curRecord : records) {
            if (i == 0) {
                consensusRecord = curRecord;
                consensusIndex = i;
                i++;
                continue;
            } else {
                if (curRecord.getPrimaryMappingQuality() + curRecord.getMateMappingQuality() > consensusRecord.getPrimaryMappingQuality() + consensusRecord.getMateMappingQuality()) {
                    consensusRecord = curRecord;
                    consensusIndex = i;
                }
            }
            i++;
        }
        records.remove(consensusIndex);

        // Get the count for the record that match, discard others
        int consensusCount = 1;
        for (IpcrRecord curRecord : records) {
            curRecord.setMateReadName(consensusRecord.getPrimaryReadName());

            if (!curRecord.getContig().equals(consensusRecord.getContig())) {
                // discard.write consensusContigMismatch
                //LOGGER.info("Contig mismatch");
                discardedOutputWriter.writeRecord(curRecord, "ContigMismatch");
                continue;
            }

            if (curRecord.getPrimaryStrand() != consensusRecord.getPrimaryStrand()) {
                // discard.write consensusContigMismatch
                //LOGGER.info("Contig mismatch");
                discardedOutputWriter.writeRecord(curRecord, "PrimaryStrandMistmatch");
                continue;
            }

            if (!isInWindowDistance(curRecord.getPrimaryStart(), consensusRecord.getPrimaryStart(), maxDistance)) {
                // dicard.write consensusPrimaryStartMismatch
                discardedOutputWriter.writeRecord(curRecord, "PositionMismatchR1Start");
                continue;
            }

            if (!isInWindowDistance(curRecord.getMateEnd(), consensusRecord.getMateEnd(), maxDistance)) {
                // dicard.write consensusMateEndMismatch
                discardedOutputWriter.writeRecord(curRecord, "PositionMismatchR2End");
                continue;
            }

            consensusCount++;
        }

        consensusRecord.setIpcrDuplicateCount(consensusCount);
        return consensusRecord;
    }


    private boolean isInWindow(int x, int start, int end) {
        if (x > start && x < end) {
            return true;
        }
        return false;
    }


    private boolean isInWindowDistance(int x, int y, int distance) {
        if (x > (y - distance) && x < (y + distance)) {
            return true;
        }

        return false;
    }
}
