package nl.umcg.suresnp.pipeline;


import nl.umcg.suresnp.pipeline.io.*;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrParseException;
import nl.umcg.suresnp.pipeline.ipcrrecords.AnnotatedIpcrRecord;
import org.apache.log4j.Logger;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.*;
import java.util.List;

import static nl.umcg.suresnp.pipeline.io.icpr.IpcrFileReader.getPerc;

public class AssignVariantAlleles {

    private static final Logger LOGGER = Logger.getLogger(AssignVariantAlleles.class);

    public static void run(IpcrToolsParameters params, IpcrOutputWriter outputWriter) throws IOException, IpcrParseException {

        String inputIPCRFile = params.getBarcodeFile();
        String inputGenotype = params.getInputVcf();

        VariantOutputFileWriter variantOutputFileWriter = new VariantOutputFileWriter(new File(params.getOutputPrefix() + ".variantFile"), false);

        // Read genotype data
        RandomAccessGenotypeData genotypeData = readGenotypeData(RandomAccessGenotypeDataReaderFormats.VCF,
                inputGenotype);

        // Read iPCR data
        List<AnnotatedIpcrRecord> ipcrRecords = IpcrFileReader.readIPCRFileCollapsed(inputIPCRFile);

        // Collect statistics
        int totalValidAlleles = 0;
        int totalInvalidAlleles = 0;
        int totalUndeterminedAlleles = 0;
        int totalIpcrRecords = 0;
        int totalVariantsInIpcrRecords = 0;
        int totalOverlappingVariants = 0;

        // Loop over all unique (collapsed) barcode fragment associations
        for (AnnotatedIpcrRecord record : ipcrRecords) {
            // Extract all variants overlapping sequenced regions
            Iterable<GeneticVariant> variantsInRange = genotypeData.getVariantsByRange(record.getReferenceSequence(), record.getStartOne(), record.getEndTwo());

            totalIpcrRecords ++;
            // Loop over all overlapping variants
            for (GeneticVariant variant : variantsInRange) {
                totalVariantsInIpcrRecords ++;
                // Only evaluate bi-allelic variants
                if (variant.isBiallelic()) {
                    if (record.positionInMappedRegion(variant.getStartPos(), variant.getStartPos() + variant.getAlternativeAlleles().getAllelesAsString().get(0).length())) {
                        totalOverlappingVariants ++;
                        record.addGeneticVariant(variant);
                    }
                }
            }

            outputWriter.writeIPCRRecord(record);
            variantOutputFileWriter.writeIPCRRecord(record);
            totalValidAlleles += record.getValidVariantAlleles();
            totalInvalidAlleles += record.getInvalidVariantAlleles();
            totalUndeterminedAlleles += record.getUndeterminedVariantAlleles();
        }

        // Close file streams
        outputWriter.close();
        genotypeData.close();

        // Log statistics
        LOGGER.info("Note: allele counts not unique, can contain duplicates");
        LOGGER.info("Total iPCR records processed: " + totalIpcrRecords);
        LOGGER.info("Total alleles in range: " + totalVariantsInIpcrRecords);
        LOGGER.info("Total alleles in sequence: " + totalOverlappingVariants + " (" + getPerc(totalOverlappingVariants, totalVariantsInIpcrRecords) + "%)");
        LOGGER.info("Total valid alleles: " + totalValidAlleles + " (" + getPerc(totalValidAlleles, totalOverlappingVariants) + "%)");
        LOGGER.info("Total invalid alleles: " + totalInvalidAlleles + " (" + getPerc(totalInvalidAlleles, totalOverlappingVariants) + "%)");
        LOGGER.info("Total undetermined alleles: " + totalUndeterminedAlleles + " (" + getPerc(totalUndeterminedAlleles, totalOverlappingVariants) + "%)" );
        LOGGER.info("Mean allele count in read: " + (float)totalValidAlleles / (float)totalIpcrRecords);
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
