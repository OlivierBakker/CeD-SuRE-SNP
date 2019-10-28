package nl.umcg.suresnp.pipeline;


import htsjdk.samtools.*;
import nl.umcg.suresnp.pipeline.barcodes.filters.AdapterSequenceMaxMismatchFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.*;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.BarcodeFileReader;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.GenericBarcodeFileReader;
import nl.umcg.suresnp.pipeline.io.icpr.DiscaredIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.icpr.AlleleSpecificIpcrOutputWriter;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrParseException;
import nl.umcg.suresnp.pipeline.ipcrrecords.AlleleSpecificIpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.VariantType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.*;
import java.util.*;

import static nl.umcg.suresnp.pipeline.ipcrrecords.VariantType.INSERTION;

public class AssignVariantAlleles {

    private static final Logger LOGGER = Logger.getLogger(AssignVariantAlleles.class);
    private AlleleSpecificIpcrOutputWriter alleleSpecificIpcrOutputWriter;
    private DiscaredIpcrRecordWriter discaredOutputWriter;
    private BarcodeFileReader barcodeFileReader;
    private AssignVariantAllelesParameters params;
    private Map<String, String> barcodeReadMap;

    public AssignVariantAlleles(AssignVariantAllelesParameters params) throws IOException {

        this.barcodeFileReader = new GenericBarcodeFileReader(params.getOutputPrefix());
        //this.ipcrOutputWriter = new GenericIpcrRecordWriter(new File(params.getOutputPrefix() + ".ipcr"), false);
        this.alleleSpecificIpcrOutputWriter = params.getOutputWriter();
        this.discaredOutputWriter = new DiscaredIpcrRecordWriter(new File(params.getOutputPrefix() + ".discarded.reads.txt"), false);
        this.params = params;
    }

    public void run() throws IOException, IpcrParseException {

        LOGGER.warn("Currently assigns any read in BAM file if possible, does not do filtering of alignment quality" +
                ",duplicates etc. so make sure you filter your BAM beforehand if needed.");

        if (params.getSampleGenotypeId() != null) {
            LOGGER.warn("Provided sample id to check genotype calls using -v. Make sure your BAM only contains the " +
                    "reads for the sample you provided, otherwise reads will be wrongly assigned to the sample provided." +
                    "If read groups have been properly marked you should be able to split the BAM in a per sample ones based on the" +
                    "@RG tags in the read headers.");
        }

        // Read barcode data
        List<InfoRecordFilter> filters = new ArrayList<>();
        filters.add(new FivePrimeFragmentLengthEqualsFilter(params.getBarcodeLength()));
        filters.add(new AdapterSequenceMaxMismatchFilter(params.getAdapterMaxMismatch()));

        barcodeReadMap = barcodeFileReader.readBarcodeFileAsStringMap(new GenericFile(params.getInputBarcodes()), filters);
        barcodeFileReader.close();

        // Create genotype data iterator
        RandomAccessGenotypeData genotypeData = readGenotypeData(RandomAccessGenotypeDataReaderFormats.VCF, params.getInputVcf());

        // Create SAM readers
        SamReader primarySamReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT).open(new File(params.getInputBam()));

        SamReader secondarySamReader = null;
        if (params.hasSecondaryInputBam()) {
            secondarySamReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT).open(new File(params.getSecondaryInputBam()));
        }

        discaredOutputWriter.writeHeader("source\treason");
        alleleSpecificIpcrOutputWriter.writeHeader("source");

        int sampleIdx = -9;
        if (params.getSampleGenotypeId() != null) {
            String[] sampleNames = genotypeData.getSampleNames();
            sampleIdx = ArrayUtils.indexOf(sampleNames, params.getSampleGenotypeId());

            if (sampleIdx == -9) {
                LOGGER.error("Sample name provided, but not found in genotype data. Make sure the sample name is correct.");
                throw new IllegalArgumentException();
            }
        }

        int i = 0;
        for (GeneticVariant curVariant : genotypeData) {

            if (!curVariant.isBiallelic()) {
                LOGGER.warn("Skipping " + curVariant.getPrimaryVariantId() + " variant is not bi-allelic");
                continue;
            }

            // Get variant information
            VariantType variantType = determineVariantType(curVariant);

            // Get the sam records overlapping the variant
            SAMRecordIterator primarySamRecordIterator = primarySamReader.queryOverlapping(curVariant.getSequenceName(), curVariant.getStartPos(), curVariant.getStartPos() + 1);
            Set<String> primaryReadNameCache = new HashSet<>();

            // Loop over all the records
            while (primarySamRecordIterator.hasNext()) {
                // Logging progress
                if (i > 0) {
                    if (i % 1000000 == 0) {
                        LOGGER.info("Processed " + i / 1000000 + " million SAM records total (primary + secondary)");
                    }
                }
                // Retrieve the current record
                SAMRecord record = primarySamRecordIterator.next();
                primaryReadNameCache.add(record.getReadName());
                AlleleSpecificIpcrRecord curAlleleSpecificIpcrRecord = assignVariantAlleleToSamRecord(record, variantType, curVariant, sampleIdx, "PrimaryBamFile");
                evaluateIpcrRecord(curAlleleSpecificIpcrRecord, "PrimaryBamFile", sampleIdx);

                i++;
            }
            primarySamRecordIterator.close();

            if (params.hasSecondaryInputBam() && secondarySamReader != null) {

                SAMRecordIterator secondarySamRecordIterator = secondarySamReader.queryOverlapping(curVariant.getSequenceName(), curVariant.getStartPos(), curVariant.getStartPos() + 1);
                while (secondarySamRecordIterator.hasNext()) {
                    SAMRecord record = secondarySamRecordIterator.next();

                    if (primaryReadNameCache.contains(record.getReadName())) {
                        discaredOutputWriter.writeRecord(new AlleleSpecificIpcrRecord(null,
                                null,
                                null,
                                0,
                                curVariant,
                                record,
                                variantType), "SecondaryBamFile\tReadInPrimaryBam");
                    } else {
                        AlleleSpecificIpcrRecord curAlleleSpecificIpcrRecord = assignVariantAlleleToSamRecord(record, variantType, curVariant, sampleIdx, "SecondaryBamFile");
                        evaluateIpcrRecord(curAlleleSpecificIpcrRecord, "SecondaryBamFile", sampleIdx);
                    }
                    i++;
                }
                secondarySamRecordIterator.close();
            }
        }

        // Close file streams
        alleleSpecificIpcrOutputWriter.flushAndClose();
        discaredOutputWriter.flushAndClose();

        genotypeData.close();

        // Log statistics
        LOGGER.info("Done");
    }

    private boolean evaluateIpcrRecord(AlleleSpecificIpcrRecord curAlleleSpecificIpcrRecord, String source, int sampleIdx) throws IOException {

        // TODO: refactor to filter pattern so filters can be provided at runtime
        // TODO: refactor/fix the ugly reason stuff
        if (curAlleleSpecificIpcrRecord != null) {
            curAlleleSpecificIpcrRecord.setSampleId(params.getSampleGenotypeId());
            // Check if the read is a artifical haplotype
            if (!curAlleleSpecificIpcrRecord.getRecord().getReadName().matches("^HC[0-9]*")) {
                // Check if the read has been marked as a duplicate alignment
                if (!curAlleleSpecificIpcrRecord.getRecord().getDuplicateReadFlag()) {
                    // Get the barcode for the read
                    String curBarcode = barcodeReadMap.get(curAlleleSpecificIpcrRecord.getRecord().getReadName());
                    // Check if the read has a barcode
                    if (curBarcode == null) {
                        discaredOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source + "\tBarcodeNotAvail");
                    } else {
                        curAlleleSpecificIpcrRecord.setBarcode(curBarcode);
                        // If the sample index is provided, use it to match the read alleles to the samples genotype
                        if (sampleIdx >= 0) {
                            int curGenotype = curAlleleSpecificIpcrRecord.getGeneticVariant().getSampleCalledDosages()[sampleIdx];
                            List<String> alleles = curAlleleSpecificIpcrRecord.getGeneticVariant().getVariantAlleles().getAllelesAsString();
                            String allele;
                            // When the genotype is HOM_REF or HOM_ALT only allow those alleles in the read, otherwise discard
                            // If HET allow any of the 2 alleles
                            // Genotype 0 corresponds to HOM for the 2nd allele in the list
                            // Genotype 2 corresponds to HOM for the 1st allele in the list
                            // This is because genotype IO assigns the dosage 2 to the FIRST allele it encounters
                            if (curGenotype == 0) {
                                allele = alleles.get(1);
                            } else if (curGenotype == 2) {
                                allele = alleles.get(0);
                            } else {
                                alleleSpecificIpcrOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source);
                                return true;
                            }
                            if (curAlleleSpecificIpcrRecord.getReadAllele().equals(allele)) {
                                alleleSpecificIpcrOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source);
                                return true;
                            } else {
                                discaredOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source + "\tReadAlleleMismatchesGenotype:" + curGenotype);
                                return false;
                            }

                        } else {
                            alleleSpecificIpcrOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source);
                            return true;
                        }
                    }
                } else {
                    discaredOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source + "\tReadMarkedAsDuplicate");
                }
            } else {
                discaredOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source + "\tArtificialHaplotype");
            }
        }

        return false;
    }


    private AlleleSpecificIpcrRecord assignVariantAlleleToSamRecord(SAMRecord samRecord, VariantType variantType, GeneticVariant curVariant, int sampleIdx, String source) throws IOException {
        // TODO: implement option maxDistToReadEnd, to only assign reads where the base is x bases in
        // TODO: implement option minBaseQualScore, to only assign a allele if the quality of that base is sufficient
        // TODO: although functional, clean up the code to make it more readable

        String referenceAllele = curVariant.getRefAllele().getAlleleAsString();
        String alternativeAllele = String.join("", curVariant.getAlternativeAlleles().getAllelesAsString());
        int variantPos = curVariant.getStartPos();

        // Get the variant position in the read
        int variantPosInRead = samRecord.getReadPositionAtReferencePosition(variantPos, true) - 1;
        if (variantPosInRead < 0) {
            variantPosInRead = 0;
        }

        // Initialize the storage variables;
        String readAllele = null;
        String altReadAllele = null;
        String read = samRecord.getReadString();
        String reason = "";
        boolean discarded = false;
        String tmpReadAllele1;
        String tmpReadAllele2;

        String expectedAlleleBasedOnSampleGenotype = null;
        if (sampleIdx >= 0) {
            int sampleDosage = curVariant.getSampleCalledDosages()[sampleIdx];

            if (sampleDosage == 2) {
                expectedAlleleBasedOnSampleGenotype = curVariant.getVariantAlleles().getAllelesAsString().get(0);
            } else if (sampleDosage == 0) {
                expectedAlleleBasedOnSampleGenotype = curVariant.getVariantAlleles().getAllelesAsString().get(1);
            }
        }

        switch (variantType) {
            case SNP:
                tmpReadAllele1 = Character.toString(read.charAt(variantPosInRead));

                if (tmpReadAllele1.equals(referenceAllele)) {
                    readAllele = tmpReadAllele1;
                    altReadAllele = alternativeAllele;
                    break;

                } else if (tmpReadAllele1.equals(alternativeAllele)) {
                    readAllele = tmpReadAllele1;
                    altReadAllele = referenceAllele;
                    break;
                } else {
                    readAllele = tmpReadAllele1;
                    altReadAllele = null;
                    reason = "NoValidAllele";
                    discarded = true;
                    break;
                }
            case INSERTION:
                tmpReadAllele1 = Character.toString(read.charAt(variantPosInRead));
                int insertEnd = variantPosInRead + alternativeAllele.length();
                // If the variant is at the end of the read and the read does not fully cover the insertion, discard the read
                // This is done to avoid false positive reference assignments
                if (insertEnd > read.length()) {
                    readAllele = Character.toString(read.charAt(variantPosInRead));
                    altReadAllele = read.substring(variantPosInRead);

                    reason = "FullAltAlleleNotAvailable";
                    discarded = true;
                    break;
                }
                tmpReadAllele2 = read.substring(variantPosInRead, insertEnd);

                // TODO: verify
                if (tmpReadAllele1.equals(expectedAlleleBasedOnSampleGenotype)) {
                    readAllele = tmpReadAllele1;
                    break;
                } else if (tmpReadAllele1.equals(referenceAllele) && !tmpReadAllele2.equals(alternativeAllele)) {
                    readAllele = tmpReadAllele1;
                    break;
                } else if (tmpReadAllele2.equals(alternativeAllele)) {
                    readAllele = tmpReadAllele2;
                    break;
                } else {
                    readAllele = tmpReadAllele1;
                    altReadAllele = tmpReadAllele2;
                    reason = "NoValidAllele";
                    discarded = true;
                    break;
                }

            case DELETION:
                int deletionEnd = variantPosInRead + referenceAllele.length();
                if (deletionEnd > read.length()) {
                    reason = "FullRefAlleleNotAvailable";
                    readAllele = read.substring(variantPosInRead);
                    altReadAllele = Character.toString(read.charAt(variantPosInRead));
                    discarded = true;
                    break;
                }
                tmpReadAllele1 = read.substring(variantPosInRead, deletionEnd);
                tmpReadAllele2 = Character.toString(read.charAt(variantPosInRead));
                // TODO: verify
                if (tmpReadAllele2.equals(expectedAlleleBasedOnSampleGenotype)) {
                    readAllele = tmpReadAllele2;
                    break;
                } else if (tmpReadAllele1.equals(referenceAllele)) {
                    readAllele = tmpReadAllele1;
                    break;
                } else if (tmpReadAllele2.equals(alternativeAllele)) {
                    readAllele = tmpReadAllele2;
                    break;
                } else {
                    readAllele = tmpReadAllele1;
                    altReadAllele = tmpReadAllele2;
                    reason = "NoValidAllele";
                    discarded = true;
                    break;
                }
            case INVALID:
                reason = "InvalidVariantType";
                discarded = true;
        }

        AlleleSpecificIpcrRecord outputRecord = new AlleleSpecificIpcrRecord(null, readAllele, altReadAllele, variantPosInRead, curVariant, samRecord, variantType);

        if (discarded) {
            discaredOutputWriter.writeRecord(outputRecord, source + "\t" + reason);
            return null;
        } else {
            return outputRecord;
        }
    }


    private static VariantType determineVariantType(GeneticVariant variant) {

        String referenceAllele = variant.getRefAllele().getAlleleAsString();
        String alternativeAllele = String.join("", variant.getAlternativeAlleles().getAllelesAsString());

        if (referenceAllele.length() == 1 && alternativeAllele.length() == 1) {
            return VariantType.SNP;
        }

        if (referenceAllele.length() == 1 && alternativeAllele.length() > 1) {
            return INSERTION;
        }

        if (referenceAllele.length() > 1 && alternativeAllele.length() == 1) {
            return VariantType.DELETION;
        }

        return VariantType.INVALID;

    }

    private static RandomAccessGenotypeData readGenotypeData(RandomAccessGenotypeDataReaderFormats format, String path) throws IOException {
        RandomAccessGenotypeData gt = null;
        LOGGER.warn("Codebase has only been tested on GATK formatted VCF files, " +
                "although in theory other VCF should work fine your milage may vary");
        switch (format) {
            case GEN:
                throw new UnsupportedOperationException("Not yet implemented");
            case GEN_FOLDER:
                throw new UnsupportedOperationException("Not yet implemented");
            case PED_MAP:
                throw new UnsupportedOperationException("Not yet implemented");
            case PLINK_BED:
                throw new UnsupportedOperationException("Not yet implemented");
            case SHAPEIT2:
                throw new UnsupportedOperationException("Not yet implemented");
            case TRITYPER:
                throw new UnsupportedOperationException("Not yet implemented");
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
