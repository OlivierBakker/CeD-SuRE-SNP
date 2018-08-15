package nl.umcg.suresnp.pipeline;


import htsjdk.samtools.*;
import nl.umcg.suresnp.pipeline.io.CSVReader;
import nl.umcg.suresnp.pipeline.io.IPCROutputFileWriter;
import nl.umcg.suresnp.pipeline.io.IPCROutputWriter;
import nl.umcg.suresnp.pipeline.io.IPCRStdoutWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class MergeBamWithBarcodes {

    private static final Logger LOGGER = Logger.getLogger(MergeBamWithBarcodes.class);

    public static void main(String[] args) {

        try {
            // Parse commandline arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(MergeBamWithBarcodesParameters.getOptions(), args);

            // Define the input files
            File inputBamFile = new File(cmd.getOptionValue("i").trim());
            File inputBarcodeFile = new File(cmd.getOptionValue("b").trim());

            // Define the output writer, either stdout or to file
            IPCROutputWriter outputWriter;
            if (cmd.hasOption("s")) {
                outputWriter = new IPCRStdoutWriter();
                // When writing to stdout do not use log4j unless there is an error
                Logger.getRootLogger().setLevel(Level.ERROR);
            } else {
                if (!cmd.hasOption("o")) {
                    LOGGER.error("-o not specified");
                    MergeBamWithBarcodesParameters.printHelp();
                    exit(1);
                }
                outputWriter = new IPCROutputFileWriter(new File(cmd.getOptionValue("o").trim()), false);
            }

            // Read and parse the barcode file
            Map<String, String> readBarcodePairs = readBarcodeInfoFile(inputBarcodeFile);

            // Open the SAM reader
            SamReader samReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(inputBamFile);

            // Check if the BAM is sorted on read name
            if (samReader.getFileHeader().getSortOrder() != SAMFileHeader.SortOrder.queryname) {
                LOGGER.error("BAM file not sorted on read name, sort it first");
                samReader.close();
                exit(1);
            }

            // Define the iterator
            SAMRecordIterator samRecordIterator = samReader.iterator();

            // Used to store the previous record in the SAM file
            SAMRecord cachedSamRecord = null;
            int i = 0;
            // Loop over all records in SAM file
            while (samRecordIterator.hasNext()) {
                // Logging
                if (i > 0) {
                    if (i % 1000000 == 0) {
                        LOGGER.info("Processed " + i / 1000000 + " million SAM records");
                    }
                }
                i ++;

                // Retrieve the current record
                SAMRecord record = samRecordIterator.next();

                // Check if the current record has a barcode associated with it
                if (readBarcodePairs.get(record.getReadName()) != null) {
                    // Check if the current record is the first in pair, if not skip to next iteration
                    if (record.getFirstOfPairFlag()) {
                        // Discard unmapped reads, unproper pairs and secondary alignments
                        if (record.getReadUnmappedFlag() || record.getMateUnmappedFlag() || !record.getProperPairFlag() || record.isSecondaryAlignment()) {
                            cachedSamRecord = null;
                            continue;
                        }
                        cachedSamRecord = record;
                        continue;
                    }
                } else {
                    cachedSamRecord = null;
                    continue;
                }

                // If the record is a secondary alignment, ignore it
                if (record.isSecondaryAlignment()) {
                    cachedSamRecord = null;
                    continue;
                }

                // If the previous record passed all checks, see if the current record is its mate
                if (cachedSamRecord != null) {
                    SAMRecord mate = record;
                    // If the current record is the mate of the previous write to the outputWriter
                    if (mate.getReadName().equals(cachedSamRecord.getReadName())) {
                        outputWriter.writeIPCRRecord(new IPCRRecord(readBarcodePairs.get(cachedSamRecord.getReadName()), cachedSamRecord, mate));
                        cachedSamRecord = null;
                    } else {
                        LOGGER.warn("Altough flagged as valid read pair, the read id's do not match. This should not happen unless the flags in the BAM are wrong.");
                        cachedSamRecord = null;
                    }
                }
            }

            // Close all the streams
            samRecordIterator.close();
            samReader.close();
            outputWriter.close();

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static Map<String, String> readBarcodeInfoFile(File inputBarcodes) throws IOException {
        // Open a new CSV reader
        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(inputBarcodes))), "\t");
        Map<String, String> readBarcodePairs = new HashMap<>();

        String[] line;
        long i = 0;
        long proper = 0;
        while ((line = reader.readNext()) != null) {
            // Logging
            if (i > 0) {
                if (i % 1000000 == 0) {
                    LOGGER.info("Read " + i / 1000000 + " million records");
                }
            }
            i++;

            if (line.length != 11) {
                continue;
            } else {
                if (Integer.parseInt(line[2]) == 20) {
                    proper++;
                    String readId = line[0].split("\\s")[0];
                    String barcode = line[4];

                    readBarcodePairs.put(readId, barcode);
                }
            }
        }
        reader.close();

        // Log some info
        LOGGER.info("Read " + proper + " valid barcode read pairs");
        LOGGER.info("Read " + readBarcodePairs.size() + " unique barcode read pairs");
        LOGGER.info(i - proper + " where invalid");

        return readBarcodePairs;
    }
}
