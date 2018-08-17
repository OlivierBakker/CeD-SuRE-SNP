package nl.umcg.suresnp.pipeline;


import nl.umcg.suresnp.pipeline.io.CSVReader;
import nl.umcg.suresnp.pipeline.io.IPCROutputFileWriter;
import nl.umcg.suresnp.pipeline.io.IPCROutputWriter;
import nl.umcg.suresnp.pipeline.io.IPCRStdoutWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.variant.GeneticVariant;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class AddAlleleInfo {

    private static final Logger LOGGER = Logger.getLogger(AddAlleleInfo.class);

    public static void run(CommandLine cmd) {

        try {
            String inputIPCRFile = cmd.getOptionValue("p").trim();
            String inputGenotype = cmd.getOptionValue("g").trim();

            RandomAccessGenotypeData genotypeData = readGenotypeData(RandomAccessGenotypeDataReaderFormats.VCF,
                    inputGenotype);

            List<AnnotatedIPCRRecord> ipcrRecords = readAndCollapseIPCRFile(inputIPCRFile);

            // Define the output writer, either stdout or to file
            IPCROutputWriter outputWriter;
            if (cmd.hasOption("s")) {
                outputWriter = new IPCRStdoutWriter();
                // When writing to stdout do not use log4j unless there is an error
                Logger.getRootLogger().setLevel(Level.ERROR);
            } else {
                if (!cmd.hasOption("o")) {
                    LOGGER.error("-o not specified");
                    IPCRToolsParameters.printHelp();
                    exit(1);
                }
                outputWriter = new IPCROutputFileWriter(new File(cmd.getOptionValue("o").trim()), false);
            }


            int totalValidAlleles = 0;
            int totalInvalidAlleles = 0;
            int totalUndeterminedAlleles = 0;

            for (AnnotatedIPCRRecord record : ipcrRecords) {
                Iterable<GeneticVariant> variantsInRange = genotypeData.getVariantsByRange(record.getReferenceSequence(), record.getStartOne(), record.getEndTwo());

                for (GeneticVariant variant : variantsInRange) {
                    if (variant.isBiallelic()) {
                        if (record.posistionInMappedRegion(variant.getStartPos(), variant.getStartPos() + variant.getAlternativeAlleles().getAllelesAsString().get(0).length())) {
                            record.addGeneticVariant(variant);
                        }
                    }
                }

                outputWriter.writeIPCRRecord(record);
                totalValidAlleles += record.getValidVariantAlleles();
                totalInvalidAlleles += record.getInvalidVariantAlleles();
                totalUndeterminedAlleles += record.getUndeterminedVariantAlleles();
            }

            outputWriter.close();
            genotypeData.close();
            LOGGER.info("Total valid alleles: " + totalValidAlleles);
            LOGGER.info("Total invalid alleles: " + totalInvalidAlleles);
            LOGGER.info("Total undetermined alleles: " + totalUndeterminedAlleles);
            LOGGER.info("Done");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            LOGGER.error("Unable to parse the iPCR file, has it been properly formatted?");
        }

    }

    private static List<AnnotatedIPCRRecord> readAndCollapseIPCRFile(String path) throws IOException {
        // May seem excessive, but allows for easy change to zipped files if needed
        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)))), " ");

        // To save time no support for a header, since we can safely define the format ourselves
        // Altough it could be easily added if needed
        // barcode, chromosome, s1, e1, s2, e2, orientation, mapq1, mapq2, cigar1, cigar2, seq1, seq2, count
        int[] collumnPositions = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0};
        String[] line;
        int i = 0;
        int previousRecord = 0;
        List<AnnotatedIPCRRecord> ipcrRecords = new ArrayList<>();

        while ((line = reader.readNext(true)) != null) {
            // Logging
            if (i > 0) {
                if (i % 1000000 == 0) {
                    LOGGER.info("Read " + i / 1000000 + " million records");
                }
            }

            if (line.length != 14) {
                LOGGER.warn("iPCR line of invalid length, has the separator been properly set?");
                continue;
            } else {
                AnnotatedIPCRRecord record = new AnnotatedIPCRRecord(line[collumnPositions[0]],
                        line[collumnPositions[1]],
                        Integer.parseInt(line[collumnPositions[2]]),
                        Integer.parseInt(line[collumnPositions[3]]),
                        Integer.parseInt(line[collumnPositions[4]]),
                        Integer.parseInt(line[collumnPositions[5]]),
                        line[collumnPositions[6]].charAt(0),
                        Integer.parseInt(line[collumnPositions[7]]),
                        Integer.parseInt(line[collumnPositions[8]]),
                        line[collumnPositions[9]],
                        line[collumnPositions[10]],
                        line[collumnPositions[11]],
                        line[collumnPositions[12]],
                        Integer.parseInt(line[collumnPositions[13]]));

                if (i > 0) {
                    if (ipcrRecords.get(previousRecord).getBarcode().equals(record.getBarcode())) {
                        if (ipcrRecords.get(previousRecord).getDuplicateCount() < record.getDuplicateCount()) {
                            ipcrRecords.set(previousRecord, record);
                        }
                    } else {
                        ipcrRecords.add(record);
                        previousRecord ++;
                    }

                } else {
                    ipcrRecords.add(record);
                }
            }

            i++;

        }
        reader.close();

        return ipcrRecords;
    }


    private static RandomAccessGenotypeData readGenotypeData(RandomAccessGenotypeDataReaderFormats format, String path) throws IOException {
        RandomAccessGenotypeData gt = null;

        switch (format) {
            case GEN:
                throw new UnsupportedOperationException("Not yet implemented");
            case GEN_FOLDER:
                throw new UnsupportedOperationException("Not yet implemented");
            case PED_MAP:
                throw new UnsupportedOperationException("Not yet implemented");
            case PLINK_BED:
                gt = RandomAccessGenotypeDataReaderFormats.PLINK_BED.createGenotypeData(path);
                LOGGER.warn("Using PLINK data, minor allele is assumed to be effect allele");
                LOGGER.warn("You can check the minor allele frequency's of the variantInfo file in regular mode to verify");
                break;
            case SHAPEIT2:
                throw new UnsupportedOperationException("Not yet implemented");
            case TRITYPER:
                gt = RandomAccessGenotypeDataReaderFormats.TRITYPER.createGenotypeData(path);
                break;
            case VCF:
                gt = RandomAccessGenotypeDataReaderFormats.VCF.createGenotypeData(path);
                break;
            case VCF_FOLDER:
                gt = RandomAccessGenotypeDataReaderFormats.VCF_FOLDER.createGenotypeData(path);
                break;
        }

        return (gt);
    }
}
