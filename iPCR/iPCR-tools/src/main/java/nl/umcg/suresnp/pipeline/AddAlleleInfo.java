package nl.umcg.suresnp.pipeline;


import nl.umcg.suresnp.pipeline.io.*;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.*;
import java.util.List;

public class AddAlleleInfo {

    private static final Logger LOGGER = Logger.getLogger(AddAlleleInfo.class);

    public static void run(CommandLine cmd, IPCROutputWriter outputWriter) throws IOException, IPCRParseException {

        String inputIPCRFile = cmd.getOptionValue("p").trim();
        String inputGenotype = cmd.getOptionValue("g").trim();

        // Read genotype data
        RandomAccessGenotypeData genotypeData = readGenotypeData(RandomAccessGenotypeDataReaderFormats.VCF,
                inputGenotype);

        // Read iPCR data
        List<AnnotatedIPCRRecord> ipcrRecords = IPCRFileReader.readIPCRFileCollapsed(inputIPCRFile);

        // Collect statistics
        int totalValidAlleles = 0;
        int totalInvalidAlleles = 0;
        int totalUndeterminedAlleles = 0;

        // Loop over all unique (collapsed) barcode fragment associations
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
