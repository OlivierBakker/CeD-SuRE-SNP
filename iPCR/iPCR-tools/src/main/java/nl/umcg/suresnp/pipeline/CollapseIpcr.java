package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.DiscardedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.GenericIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

            // Write the output
            outputWriter.setBarcodeCountFilesSampleNames(provider.getSamples());
            outputWriter.writeHeader();

            for (IpcrRecord record : outputList) {
                outputWriter.writeRecord(record);
            }

            outputWriter.flushAndClose();
            discardedOutputWriter.flushAndClose();
            LOGGER.info("Done, wrote " + outputList.size() + " valid collapsed records");
        } catch (IOException e) {
            e.printStackTrace();
        }
;

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

            if (!isInWindow(curRecord.getPrimaryStart(), consensusRecord.getPrimaryStart(), maxDistance)) {
                // dicard.write consensusPrimaryStartMismatch
                discardedOutputWriter.writeRecord(curRecord, "PositionMismatchR1Start");
                continue;
            }

            if (!isInWindow(curRecord.getMateEnd(), consensusRecord.getMateEnd(), maxDistance)) {
                // dicard.write consensusMateEndMismatch
                discardedOutputWriter.writeRecord(curRecord, "PositionMismatchR2End");
                continue;
            }

            consensusCount++;
        }

        consensusRecord.setIpcrDuplicateCount(consensusCount);
        return consensusRecord;
    }


    private boolean isInWindow(int x, int y, int distance) {
        if (x > y - distance && x < y + distance) {
            return true;
        }

        return false;
    }
}
