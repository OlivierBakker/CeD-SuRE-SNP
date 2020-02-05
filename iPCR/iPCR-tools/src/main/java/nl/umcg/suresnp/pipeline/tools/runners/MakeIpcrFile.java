package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.*;
import nl.umcg.suresnp.pipeline.inforecords.filters.AdapterSequenceMaxMismatchFilter;
import nl.umcg.suresnp.pipeline.inforecords.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.inforecords.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.infofilereader.GenericInfoFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.DiscardedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.ipcrrecords.SamBasedIpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.ipcrrecords.filters.IpcrRecordFilter;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeIpcrFileParameters;
import nl.umcg.suresnp.pipeline.utils.B37GenomeInfo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;
import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class MakeIpcrFile {

    private static final Logger LOGGER = Logger.getLogger(MakeIpcrFile.class);
    private MakeIpcrFileParameters params;
    private IpcrOutputWriter discardedOutputWriter;
    private IpcrOutputWriter outputWriter;
    private GenericInfoFileReader barcodeFileReader;

    public MakeIpcrFile(MakeIpcrFileParameters params) throws IOException {
        this.params = params;
        this.discardedOutputWriter = new DiscardedIpcrRecordWriter(new File(params.getOutputPrefix() + ".discarded.reads.txt"), false);
        this.outputWriter = params.getOutputWriter();
        this.barcodeFileReader = new GenericInfoFileReader(params.getOutputPrefix());
    }

    public void run() {

        try {
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

            // Read barcode count files
            Map<String, Map<String, Integer>> readBarcodeCounts = readBarcodeCountFiles();

            // Read barcode fragment pairs
            Map<String, String> readBarcodePairs = readBarcodeFragmentPairs();

            // Region filter
            IpcrRecordFilter inRegionFilter = null;
            if (params.getRegionFilterFile() != null) {
                inRegionFilter = new InRegionFilter(params.getRegionFilterFile());
            }

            // Init variables for logging some statistics
            int filterPassCount = 0;
            int filterFailCount = 0;
            int noBarcodeCount = 0;
            int missingMateCount = 0;
            int notInRegionCount = 0;

            // Define the iterator
            SAMRecordIterator samRecordIterator = samReader.iterator();

            // Used to store the previous record in the SAM file
            SAMRecord validCachedSamRecord = null;

            int i = 0;

            // Loop over all records in SAM file
            while (samRecordIterator.hasNext()) {
                logProgress(i, 1000000, "MakeIpcrFile");
                i++;

                // Retrieve the current record
                SAMRecord record = samRecordIterator.next();

                if (validCachedSamRecord == null) {
                    // Check if the current record has a barcode associated with it
                    if (readBarcodePairs.get(record.getReadName()) != null) {
                        if (isValidCachableSamRecord(record, readBarcodePairs)) {
                            validCachedSamRecord = record;
                        } else {
                            filterFailCount++;
                        }
                    } else {
                        noBarcodeCount++;
                        discardedOutputWriter.writeRecord(new SamBasedIpcrRecord("NA", record), "noBarcode");
                        validCachedSamRecord = null;
                    }
                } else {
                    // If the previous record passed all checks, see if the current record is its mate
                    SAMRecord mate = record;

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

                        SamBasedIpcrRecord curIcprRecord;
                        if (currentBarcodeCounts == null) {
                            curIcprRecord = new SamBasedIpcrRecord(barcode, validCachedSamRecord, mate);
                        } else {
                            curIcprRecord = new SamBasedIpcrRecord(barcode, validCachedSamRecord, mate, currentBarcodeCounts);
                        }

                        // Filter on the regions
                        if (inRegionFilter != null) {
                            if (inRegionFilter.passesFilter(curIcprRecord)) {
                                outputWriter.writeRecord(curIcprRecord);
                                filterPassCount ++;
                            } else {
                                notInRegionCount ++;
                            }

                        } else {
                            outputWriter.writeRecord(curIcprRecord);
                            filterPassCount ++;
                        }

                        // Reset as a valid pair has been written
                        validCachedSamRecord = null;

                    } else {
                        // This can happen if either the R1 or R2 has been filtered but not both.
                        // If this is the case, check if the current read (mate) is valid and put that as the cached read
                        discardedOutputWriter.writeRecord(new SamBasedIpcrRecord(readBarcodePairs.get(validCachedSamRecord.getReadName()), validCachedSamRecord, mate), "mateNameMismatch");
                        missingMateCount++;

                        // Check if the current record has a barcode associated with it
                        if (readBarcodePairs.get(mate.getReadName()) != null) {
                            if (isValidCachableSamRecord(mate, readBarcodePairs)) {
                                validCachedSamRecord = mate;
                            } else {
                                validCachedSamRecord = null;
                                filterFailCount++;
                            }
                        } else {
                            noBarcodeCount++;
                            discardedOutputWriter.writeRecord(new SamBasedIpcrRecord("NA", mate), "noBarcode");
                            validCachedSamRecord = null;
                        }
                    }
                }
            }
            System.out.print("\n"); // Flush progress bar

            // Log some info
            LOGGER.info("Processed a total of " + i + " reads, " + i / 2 + " pairs" );
            LOGGER.info(filterPassCount + " (" + getPerc(filterPassCount, i / 2) + "%) valid ipcr records written");
            LOGGER.info(notInRegionCount + " (" + getPerc(notInRegionCount, i / 2) + "%) pairs where filtered for not overlapping a region");
            LOGGER.info(filterFailCount + " (" + getPerc(filterFailCount, i) + "%) reads failed filtering. Either unmapped, secondary alignment or improper pair");
            LOGGER.info(noBarcodeCount + " (" + getPerc(noBarcodeCount, i) + "%) reads where in the BAM but could not be associated to a valid barcode");
            LOGGER.info(missingMateCount + " (" + getPerc(missingMateCount, i) + "%) reads where valid but missed mate");

            // Close all the streams
            samRecordIterator.close();
            samReader.close();
            outputWriter.flushAndClose();
            discardedOutputWriter.flushAndClose();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Map<String, Integer>> readBarcodeCountFiles() throws IOException {
        Map<String, Map<String, Integer>> readBarcodeCounts = null;

        if (params.hasBarcodeCountFiles()) {
            readBarcodeCounts = new HashMap<>();
            for (String file : params.getBarcodeCountFiles()) {
                GenericFile curFile = new GenericFile(file);
                readBarcodeCounts.put(curFile.getBaseName(), barcodeFileReader.readBarcodeCountFile(curFile));
            }
        }
        return readBarcodeCounts;
    }

    private Map<String, String> readBarcodeFragmentPairs() throws IOException {
        // Read and parse the barcode file
        GenericFile inputBarcodeFile = new GenericFile(params.getInputBarcodes());

        // Read barcode data
        List<InfoRecordFilter> filters = new ArrayList<>();
        filters.add(new FivePrimeFragmentLengthEqualsFilter(params.getBarcodeLength()));
        filters.add(new AdapterSequenceMaxMismatchFilter(params.getAdapterMaxMismatch()));
        Map<String, String> readBarcodePairs = barcodeFileReader.readBarcodeFileAsStringMap(inputBarcodeFile, filters);
        barcodeFileReader.flushAndClose();

        return readBarcodePairs;
    }


    // TODO: refactor to filter pattern
    private boolean isValidCachableSamRecord(SAMRecord record, Map<String, String> readBarcodePairs) throws IOException {
        // Check if the current record is the first in pair, if not skip to next iteration
/*        if (!record.getFirstOfPairFlag()) {
            discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(record.getReadName()), record), "notFirstInPair");
            return false;
        }*/
        // If the pair is not proper, ignore it
        if (!record.getProperPairFlag()) {
            discardedOutputWriter.writeRecord(new SamBasedIpcrRecord(readBarcodePairs.get(record.getReadName()), record), "notProperPair");
            return false;
        }
        // Discard non chromosomal reads
        if (!B37GenomeInfo.isChromosome(record.getContig())) {
            discardedOutputWriter.writeRecord(new SamBasedIpcrRecord(readBarcodePairs.get(record.getReadName()), record), "nonChromosomalRead");
            return false;
        }
        // If the record is a secondary alignment, ignore it
        if (record.isSecondaryAlignment()) {
            discardedOutputWriter.writeRecord(new SamBasedIpcrRecord(readBarcodePairs.get(record.getReadName()), record), "secondaryAlignment");
            return false;
        }
        // If the record is unmapped, ignore it
        if (record.getReadUnmappedFlag()) {
            discardedOutputWriter.writeRecord(new SamBasedIpcrRecord(readBarcodePairs.get(record.getReadName()), record), "unmapped");
            return false;
        }
        // If the mate is unmapped, ignore it
        if (record.getMateUnmappedFlag()) {
            discardedOutputWriter.writeRecord(new SamBasedIpcrRecord(readBarcodePairs.get(record.getReadName()), record), "mateUnmapped");
            return false;
        }
        return true;
    }


    public static int getPerc(int a, int b) {
        float p = ((float) a / (float) b) * 100;
        return Math.round(p);
    }

}
