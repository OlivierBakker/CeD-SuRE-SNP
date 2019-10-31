package nl.umcg.suresnp.pipeline;

import htsjdk.samtools.*;
import nl.umcg.suresnp.pipeline.barcodes.filters.AdapterSequenceMaxMismatchFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.GenericBarcodeFileReader;
import nl.umcg.suresnp.pipeline.io.icpr.DiscardedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

public class MergeBamWithBarcodeCounts {

    private static final Logger LOGGER = Logger.getLogger(MergeBamWithBarcodeCounts.class);
    private MergeBamWithBarcodeCountsParameters params;
    private IpcrOutputWriter discardedOutputWriter;
    private IpcrOutputWriter outputWriter;
    private GenericBarcodeFileReader barcodeFileReader;
    private Map<String, String> readBarcodePairs;

    public MergeBamWithBarcodeCounts(MergeBamWithBarcodeCountsParameters params) throws IOException {
        this.params = params;
        this.discardedOutputWriter = new DiscardedIpcrRecordWriter(new File(params.getOutputPrefix() + ".discarded.reads.txt"), false);
        this.outputWriter = params.getOutputWriter();
        this.barcodeFileReader = new GenericBarcodeFileReader(params.getOutputPrefix());
    }

    public void run() {

        try {
            // Quick and dirty implementation to merge alignment data with barcode counts.
            // Define the input files
            File inputBamFile = new File(params.getInputBam());

            // Open the SAM reader
            SamReader samReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(inputBamFile);

            // Check if the BAM is sorted on read name, if not flushAndClose the program
            // The logic requires the BAM to be sorted on read name, for efficiency's sake.
            // Given no sorting on read name, to get the mate the BAM needs to be searched.
            // To search for a mate is very slow, in testing slower then first sorting, then
            // merging. It might be that this is not the case for larger BAMs
            if (samReader.getFileHeader().getSortOrder() != SAMFileHeader.SortOrder.queryname) {
                LOGGER.error("BAM file not sorted on read name, sort it first");
                samReader.close();
                exit(1);
            }

            outputWriter.writeHeader();
            discardedOutputWriter.writeHeader("reason");

            // Read barcode count filesq
            Map<String, Map<String, Integer>> readBarcodeCounts = null;

            if (params.hasBarcodeCountFiles()) {
                readBarcodeCounts = new HashMap<>();
                for (String file : params.getBarcodeCountFiles()) {
                    String basename = FilenameUtils.getBaseName(file);
                    readBarcodeCounts.put(basename, barcodeFileReader.readBarcodeCountFile(new GenericFile(file)));
                }
            }

            // Read and parse the barcode file
            GenericFile inputBarcodeFile = new GenericFile(params.getInputBarcodes());
            //File inputBarcodeCountFile = new File(params.getInputBarcodeCounts());
            // Read barcode data
            List<InfoRecordFilter> filters = new ArrayList<>();
            filters.add(new FivePrimeFragmentLengthEqualsFilter(params.getBarcodeLength()));
            filters.add(new AdapterSequenceMaxMismatchFilter(params.getAdapterMaxMismatch()));
            readBarcodePairs = barcodeFileReader.readBarcodeFileAsStringMap(inputBarcodeFile, filters);
            barcodeFileReader.close();

            // Init variables for logging some statistics
            int filterFailCount = 0;
            int noBarcodeCount = 0;
            int missingMateCount = 0;
            int strandMismatchCount = 0;
            // Define the iterator
            SAMRecordIterator samRecordIterator = samReader.iterator();

            // Used to store the previous record in the SAM file
            SAMRecord validCachedSamRecord = null;

            int i = 0;

            // Loop over all records in SAM file
            while (samRecordIterator.hasNext()) {
                // Logging progress
                if (i > 0) {
                    if (i % 1000000 == 0) {
                        LOGGER.info("Processed " + i / 1000000 + " million SAM records");
                    }
                }
                i++;

                // Retrieve the current record
                SAMRecord record = samRecordIterator.next();

                if (validCachedSamRecord == null) {
                    // Check if the current record has a barcode associated with it
                    if (readBarcodePairs.get(record.getReadName()) != null) {
                        if (isValidCachableSamRecord(record)) {
                            validCachedSamRecord = record;
                        } else {
                            filterFailCount++;
                        }
                    } else {
                        noBarcodeCount++;
                        discardedOutputWriter.writeRecord(new IpcrRecord("NA", record), "noBarcode");
                        validCachedSamRecord = null;
                    }
                } else {
                    // If the previous record passed all checks, see if the current record is its mate
                    SAMRecord mate = record;

                    if (mate.getMateNegativeStrandFlag() != validCachedSamRecord.getMateNegativeStrandFlag()) {
                        strandMismatchCount++;
                        //discardedOutputWriter.writeRecord(new IpcrRecord("NA", mate), "strandMismatch");
                        //validCachedSamRecord = null;
                    }

                    if (mate.getReadName().equals(validCachedSamRecord.getReadName())) {
                        // If the current record is the mate of the previous write to the outputWriter
                        String barcode = readBarcodePairs.get(validCachedSamRecord.getReadName());

                        // If barcode count files have been provided add the barcode counts
                        // Needs to be a loop to accept multiple files
                        Map<String, Integer> currentBarcodeCounts = null;
                        if (params.hasBarcodeCountFiles()) {
                            currentBarcodeCounts = new HashMap<>();
                            for (String barcodeFile : readBarcodeCounts.keySet()) {
                                Integer curCount = readBarcodeCounts.get(barcodeFile).get(barcode);
                                if (curCount == null) {
                                    currentBarcodeCounts.put(barcodeFile, 0);
                                } else {
                                    currentBarcodeCounts.put(barcodeFile, curCount);
                                }
                            }
                        }

                        IpcrRecord curIcprRecord;
                        if (currentBarcodeCounts == null) {
                            curIcprRecord = new IpcrRecord(barcode, validCachedSamRecord, mate);
                        } else {
                            curIcprRecord = new IpcrRecord(barcode, validCachedSamRecord, mate, currentBarcodeCounts);
                        }
                        outputWriter.writeRecord(curIcprRecord);

                        // Reset as a valid pair has been written
                        validCachedSamRecord = null;

                    } else {
                        // This can happen if either the R1 or R2 has been filtered but not both.
                        // If this is the case, check if the current read (mate) is valid and put that as the cached read
                        discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(validCachedSamRecord.getReadName()), validCachedSamRecord, mate), "mateNameMismatch");
                        missingMateCount++;

                        // Check if the current record has a barcode associated with it
                        if (readBarcodePairs.get(mate.getReadName()) != null) {
                            if (isValidCachableSamRecord(mate)) {
                                validCachedSamRecord = mate;
                            } else {
                                validCachedSamRecord = null;
                                filterFailCount++;
                            }
                        } else {
                            noBarcodeCount++;
                            discardedOutputWriter.writeRecord(new IpcrRecord("NA", mate), "noBarcode");
                            validCachedSamRecord = null;
                        }
                    }
                }
            }

            // Log some info
            LOGGER.info("Processed a total of " + i + " reads");
            LOGGER.info(strandMismatchCount + " (" + getPerc(strandMismatchCount, i/2) + "%) pairs with opposing strands");
            LOGGER.info(filterFailCount + " (" + getPerc(filterFailCount, i) + "%) reads failed filtering. Either unmapped, secondary alignment or improper pair");
            LOGGER.info(noBarcodeCount + " (" + getPerc(noBarcodeCount, i) + "%) reads where in the BAM but could not be associated to a barcode");
            LOGGER.info(missingMateCount + " (" + getPerc(missingMateCount, i) + "%) reads where valid but missed mate");

            // Close all the streams
            samRecordIterator.close();
            samReader.close();
            outputWriter.flushAndClose();
            discardedOutputWriter.flushAndClose();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: refactor to filter pattern
    private boolean isValidCachableSamRecord(SAMRecord record) throws IOException {
        // Check if the current record is the first in pair, if not skip to next iteration
/*        if (!record.getFirstOfPairFlag()) {
            discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(record.getReadName()), record), "notFirstInPair");
            return false;
        }*/
        // If the pair is not proper, ignore it
        if (!record.getProperPairFlag()) {
            discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(record.getReadName()), record), "notProperPair");
            return false;
        }
        // Discard non chromosomal reads
        if (!isChromosome(record.getContig())) {
            discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(record.getReadName()), record), "nonChromosomalRead");
            return false;
        }
        // If the record is a secondary alignment, ignore it
        if (record.isSecondaryAlignment()) {
            discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(record.getReadName()), record), "secondaryAlignment");
            return false;
        }
        // If the record is unmapped, ignore it
        if (record.getReadUnmappedFlag()) {
            discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(record.getReadName()), record), "unmapped");
            return false;
        }
        // If the mate is unmapped, ignore it
        if (record.getMateUnmappedFlag()) {
            discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(record.getReadName()), record), "mateUnmapped");
            return false;
        }
        return true;
    }


    public static boolean isChromosome(String contig) {
        return (MergeBamWithBarcodeCountsParameters.getChromosomes().contains(contig));
    }


    public static int getPerc(int a, int b) {
        float p = ((float) a / (float) b) * 100;
        return Math.round(p);
    }

}