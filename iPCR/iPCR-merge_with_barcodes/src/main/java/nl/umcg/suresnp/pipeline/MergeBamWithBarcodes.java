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

            File inputBam = new File(cmd.getOptionValue("i").trim());
            File inputBarcodes = new File(cmd.getOptionValue("b").trim());

            IPCROutputWriter outputWriter;
            if (cmd.hasOption("s")) {
                outputWriter = new IPCRStdoutWriter();
                Logger.getRootLogger().setLevel(Level.ERROR);
            } else {
                if (!cmd.hasOption("o")) {
                    LOGGER.error("-o not specified");
                    MergeBamWithBarcodesParameters.printHelp();
                    exit(1);
                }
                File outputFile = new File(cmd.getOptionValue("o").trim());
                outputWriter = new IPCROutputFileWriter(outputFile, false);
            }
            CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(inputBarcodes))), "\t");

            Map<String, String> readBarcodePairs = new HashMap<>();

            String[] line;
            long i = 0;
            long proper = 0;
            while ((line = reader.readNext()) != null) {

                if (i > 0) {
                    if (i % 1000000 == 0) {
                        LOGGER.info("Read " + i / 1000000 + " million records");
                    }
                }

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
                i++;
            }

            LOGGER.info("Read " + proper + " valid barcode read pairs");
            LOGGER.info("Read " + readBarcodePairs.size() + " unique barcode read pairs");
            LOGGER.info(i - proper + " where invalid");

            SamReader samReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(inputBam);

            if (samReader.getFileHeader().getSortOrder() != SAMFileHeader.SortOrder.queryname) {
                LOGGER.error("BAM file not sorted on query name, sort it first");
                samReader.close();
                exit(1);
            }

            SAMRecordIterator samRecordIterator = samReader.iterator();
            SAMRecord cachedSamRecord = null;
            i = 0;
            while (samRecordIterator.hasNext()) {
                SAMRecord record = samRecordIterator.next();

                if (i > 0) {
                    if (i % 1000000 == 0) {
                        LOGGER.info("Processed " + i / 1000000 + " million SAM records");
                    }
                }

                i ++;
                if (readBarcodePairs.get(record.getReadName()) != null) {
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

                if (record.isSecondaryAlignment()) {
                    cachedSamRecord = null;
                    continue;
                }

                if (cachedSamRecord != null) {
                    SAMRecord mate = record;
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
            reader.close();
            outputWriter.close();

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
