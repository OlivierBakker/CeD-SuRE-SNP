package nl.umcg.suresnp.pipeline;

import htsjdk.samtools.*;
import nl.umcg.suresnp.pipeline.barcodes.filters.AdapterSequenceMaxMismatchFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.CsvReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.GenericBarcodeFileReader;
import nl.umcg.suresnp.pipeline.io.icpr.*;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import java.io.*;
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

            // Read and parse the barcode file
            GenericFile inputBarcodeFile = new GenericFile(params.getInputBarcodes());
            //File inputBarcodeCountFile = new File(params.getInputBarcodeCounts());
            // Read barcode data
            List<InfoRecordFilter> filters = new ArrayList<>();
            filters.add(new FivePrimeFragmentLengthEqualsFilter(params.getBarcodeLength()));
            filters.add(new AdapterSequenceMaxMismatchFilter(params.getAdapterMaxMismatch()));
            Map<String, String> readBarcodePairs = barcodeFileReader.readBarcodeFileAsStringMap(inputBarcodeFile, filters);

            //Map<String, Integer> readBarcodeCounts = barcodeFileReader.readBarcodeCountFile(inputBarcodeCountFile);
            barcodeFileReader.close();

            // Init variables for logging some statistics
            int filterFailCount = 0;
            int noBarcodeCount = 0;

            // Define the iterator
            SAMRecordIterator samRecordIterator = samReader.iterator();

            // Used to store the previous record in the SAM file
            SAMRecord cachedSamRecord = null;
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

                // Check if the current record has a barcode associated with it
                if (readBarcodePairs.get(record.getReadName()) != null) {
                    // Check if the current record is the first in pair, if not skip to next iteration
                    if (record.getFirstOfPairFlag()) {
                        // Discard unmapped reads, unproper pairs and secondary alignments
                        if (record.getReadUnmappedFlag() || record.getMateUnmappedFlag() || !record.getProperPairFlag() || record.isSecondaryAlignment()) {
                            filterFailCount++;
                            discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(cachedSamRecord.getReadName()), cachedSamRecord), "unmapped|mateUnmapped|!properPair|secondary");
                            cachedSamRecord = null;
                            continue;
                        }

                        cachedSamRecord = record;
                        continue;
                    }
                } else {
                    noBarcodeCount++;
                    discardedOutputWriter.writeRecord(new IpcrRecord("NA", cachedSamRecord), "noBarcode");
                    cachedSamRecord = null;
                    continue;
                }

                // If the record is a secondary alignment, ignore it
                if (record.isSecondaryAlignment()) {
                    discardedOutputWriter.writeRecord(new IpcrRecord(readBarcodePairs.get(cachedSamRecord.getReadName()), cachedSamRecord), "secondaryAlignment");
                    cachedSamRecord = null;
                    continue;
                }

                // If the previous record passed all checks, see if the current record is its mate
                if (cachedSamRecord != null) {
                    SAMRecord mate = record;
                    // If the current record is the mate of the previous write to the outputWriter
                    if (mate.getReadName().equals(cachedSamRecord.getReadName())) {
                        String barcode = readBarcodePairs.get(cachedSamRecord.getReadName());
                        // Needs to be a loop to accept multiple files
                        IpcrRecord curIcprRecord = new IpcrRecord(barcode, cachedSamRecord, mate);
                        outputWriter.writeRecord(curIcprRecord);

                        cachedSamRecord = null;
                    } else {
                        LOGGER.warn("Altough flagged as valid read pair, the read id's do not match. This should not happen unless the flags in the BAM are wrong.");
                        cachedSamRecord = null;
                    }
                }
            }

            // Log some info
            LOGGER.info("Processed a total of " + i + " reads");
            LOGGER.info(filterFailCount + " (" + getPerc(filterFailCount, i) + "%) reads failed filtering. Either unmapped, secondary alignment or improper pair");
            LOGGER.info(noBarcodeCount + " (" + getPerc(noBarcodeCount, i) + "%) reads where in the BAM but could not be associated to a barcode");

            // Close all the streams
            samRecordIterator.close();
            samReader.close();
            outputWriter.flushAndClose();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static int getPerc(int a, int b) {
        float p = ((float) a / (float) b) * 100;
        return Math.round(p);
    }

}
