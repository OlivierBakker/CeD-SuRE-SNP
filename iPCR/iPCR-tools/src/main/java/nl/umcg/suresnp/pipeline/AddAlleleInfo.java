package nl.umcg.suresnp.pipeline;


import nl.umcg.suresnp.pipeline.io.*;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddAlleleInfo {

    private static final Logger LOGGER = Logger.getLogger(AddAlleleInfo.class);

    public static void run(CommandLine cmd, IPCROutputWriter outputWriter) throws IOException, IPCRParseException {

        LOGGER.warn("Current implementation assumes file is sorted on barcode");

        String inputIPCRFile = cmd.getOptionValue("p").trim();
        String inputGenotype = cmd.getOptionValue("g").trim();

        // Read genotype data
        RandomAccessGenotypeData genotypeData = readGenotypeData(RandomAccessGenotypeDataReaderFormats.VCF,
                inputGenotype);

        // Read iPCR data
        List<AnnotatedIPCRRecord> ipcrRecords = readIPCRFileCollapsed(inputIPCRFile);

        // Collect statistics
        int totalValidAlleles = 0;
        int totalInvalidAlleles = 0;
        int totalUndeterminedAlleles = 0;

        // Loop over all unique barcode fragment associations
        for (AnnotatedIPCRRecord record : ipcrRecords) {
            // Extract all variants overlapping sequenced regions
            Iterable<GeneticVariant> variantsInRange = genotypeData.getVariantsByRange(record.getReferenceSequence(), record.getStartOne(), record.getEndTwo());

            // Loop over all overlapping variants
            for (GeneticVariant variant : variantsInRange) {
                // Only evaluate bi-allelic variants
                if (variant.isBiallelic()) {
                    if (record.positionInMappedRegion(variant.getStartPos(), variant.getStartPos() + variant.getAlternativeAlleles().getAllelesAsString().get(0).length())) {
                        record.addGeneticVariant(variant);
                    }
                }
            }

            outputWriter.writeIPCRRecord(record);
            totalValidAlleles += record.getValidVariantAlleles();
            totalInvalidAlleles += record.getInvalidVariantAlleles();
            totalUndeterminedAlleles += record.getUndeterminedVariantAlleles();
        }

        // Close file streams
        outputWriter.close();
        genotypeData.close();

        // Log statistics
        LOGGER.info("Total valid alleles: " + totalValidAlleles);
        LOGGER.info("Total invalid alleles: " + totalInvalidAlleles);
        LOGGER.info("Total undetermined alleles: " + totalUndeterminedAlleles);
        LOGGER.info("Done");

    }

    private static List<AnnotatedIPCRRecord> readIPCRFileCollapsed(String path) throws IOException, IPCRParseException {

        // May seem excessive, but allows for easy change to zipped files if needed
        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)))), " ");

        // To save time no support for a header, since we can safely define the format ourselves
        // Although it could be easily added if needed
        // Column order:
        // barcode, chromosome, s1, e1, s2, e2, orientation, mapq1, mapq2, cigar1, cigar2, seq1, seq2, count
        int[] ipcrColumnOrder = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0};
        List<AnnotatedIPCRRecord> ipcrRecords = new ArrayList<>();
        String[] line;
        int curRecord = 0;
        int prevRecord = 0;

        while ((line = reader.readNext(true)) != null) {
            // Logging
            if (curRecord > 0){if(curRecord % 1000000 == 0){LOGGER.info("Read " + curRecord / 1000000 + " million records");}}

            if (line.length != 14) {
                LOGGER.warn("Skipping line" + curRecord + " since it is of invalid length, has the separator been properly set?");
                continue;
            } else {
                // Parse the iPCR record
                AnnotatedIPCRRecord record;
                try {
                     record = new AnnotatedIPCRRecord(line[ipcrColumnOrder[0]],
                            line[ipcrColumnOrder[1]],
                            Integer.parseInt(line[ipcrColumnOrder[2]]),
                            Integer.parseInt(line[ipcrColumnOrder[3]]),
                            Integer.parseInt(line[ipcrColumnOrder[4]]),
                            Integer.parseInt(line[ipcrColumnOrder[5]]),
                            line[ipcrColumnOrder[6]].charAt(0),
                            Integer.parseInt(line[ipcrColumnOrder[7]]),
                            Integer.parseInt(line[ipcrColumnOrder[8]]),
                            line[ipcrColumnOrder[9]],
                            line[ipcrColumnOrder[10]],
                            line[ipcrColumnOrder[11]],
                            line[ipcrColumnOrder[12]],
                            Integer.parseInt(line[ipcrColumnOrder[13]]));
                } catch (NumberFormatException e) {
                    throw new IPCRParseException(e.getMessage(), curRecord);
                }

                // Collapse the records on barcode, keeping only the one with the highest duplicate count
                if (curRecord > 0) {
                    if (ipcrRecords.get(prevRecord).getBarcode().equals(record.getBarcode())) {
                        if (ipcrRecords.get(prevRecord).getDuplicateCount() < record.getDuplicateCount()) {
                            ipcrRecords.set(prevRecord, record);
                        }
                    } else {
                        ipcrRecords.add(record);
                        prevRecord++;
                    }

                } else {
                    ipcrRecords.add(record);
                }
            }

            curRecord++;

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
