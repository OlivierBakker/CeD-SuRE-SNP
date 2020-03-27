package nl.umcg.suresnp.pipeline.tools.runners;


import htsjdk.samtools.*;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BlockCompressedIpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.AlleleSpecificIpcrOutputWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.DiscaredAlleleSpecificIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrParseException;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.*;
import nl.umcg.suresnp.pipeline.records.samrecord.PairedSamRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.AssignVariantAllelesParameters;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static nl.umcg.suresnp.pipeline.records.ipcrrecord.VariantType.INSERTION;

public class AssignVariantAlleles {
    private static final Logger LOGGER = Logger.getLogger(AssignVariantAlleles.class);
    private AssignVariantAllelesParameters params;

    // Input
    private SamReader primarySamReader;
    private SamReader secondarySamReader;
    private BlockCompressedIpcrFileReader ipcrReader;

    // Output
    private AlleleSpecificIpcrOutputWriter outputWriter;
    private DiscaredAlleleSpecificIpcrRecordWriter discaredOutputWriter;

    public AssignVariantAlleles(AssignVariantAllelesParameters params) throws IOException {
        this.params = params;

        // Input iPCR reader
        this.ipcrReader = new BlockCompressedIpcrFileReader(new GenericFile(params.getInputIpcr()));

        // Create SAM readers
        this.primarySamReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT).open(new File(params.getInputBam()));

        this.secondarySamReader = null;
        if (this.params.hasSecondaryInputBam()) {
            secondarySamReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT).open(new File(params.getSecondaryInputBam()));
        }

        // Output writers
        this.outputWriter = params.getOutputWriter();
        this.outputWriter.setBarcodeCountFilesSampleNames(ipcrReader.getCdnaSamples());
        this.discaredOutputWriter = new DiscaredAlleleSpecificIpcrRecordWriter(new File(params.getOutputPrefix() + ".discarded.reads.txt"), false);
    }

    public void run() throws IOException, IpcrParseException {
        //TODO: figure out what the bug is > is there a bug, why did I put this here? looked trough the code and it looks good to me
        if (params.getSampleGenotypeId() != null) {
            LOGGER.warn("Provided sample id to check genotype calls using -v. Make sure your BAM only contains the " +
                    "reads for the sample you provided, otherwise reads will be wrongly assigned to the sample provided." +
                    "If read groups have been properly marked you should be able to split the BAM in a per sample ones based on the" +
                    "@RG tags in the read headers.");
        }

        // Create genotype data iterator
        RandomAccessGenotypeData genotypeData = readGenotypeData();

        discaredOutputWriter.writeHeader("source\treason");

        if (!params.getOutputType().equals("MINIMAL")) {
            outputWriter.writeHeader("source");
        }

        // Determine the index of the sample to check genotypes against
        int sampleIdx = -9;
        if (params.getSampleGenotypeId() != null) {
            String[] sampleNames = genotypeData.getSampleNames();
            sampleIdx = ArrayUtils.indexOf(sampleNames, params.getSampleGenotypeId());

            if (sampleIdx == -9) {
                LOGGER.error("Sample name provided, but not found in genotype data. Make sure the sample name is correct.");
                throw new IllegalArgumentException();
            }
        }

        // Loop over all genetic variants in VCF
        for (GeneticVariant curVariant : genotypeData) {
            if (!curVariant.isBiallelic()) {
                LOGGER.warn("Skipping " + curVariant.getPrimaryVariantId() + " variant is not bi-allelic");
                continue;
            }

            // SAM records, used to get sequence and potentially updated alignment info
            Map<String, PairedSamRecord> currentSamRecords = getOverlappingSamRecords(curVariant);

            // Get iPCR records overlapping a variant. Is done in window alignment info maybe shifted by GATK
            // Inside an active window.
            Map<String, IpcrRecord> currentIpcrRecords = getOverlappingIpcrRecords(curVariant, 500);

            // Determine shared reads between iPCR and BAM (should be equal or iPCR should be greater)
            // as the iPCR indicates the collapsed unique reads
            Set<String> overlappingReads = new HashSet<>(currentIpcrRecords.keySet());
            overlappingReads.retainAll(currentSamRecords.keySet());
            //LOGGER.debug(overlappingReads.size() + " are intersecting");

            for (String curRead : overlappingReads) {
                // Assign a allele to the current read
                AlleleSpecificIpcrRecord rec = assignVariantAlleleToIpcrRecord(
                        currentIpcrRecords.get(curRead),
                        currentSamRecords.get(curRead),
                        curVariant,
                        sampleIdx,
                        currentSamRecords.get(curRead).getSource());

                // Update position if needed since GATK does local re-alignment
                // TODO: implement this
               // rec.updatePositions(currentSamRecords.get(curRead));
                evaluateIpcrRecord(rec, currentSamRecords.get(curRead).getSource(), sampleIdx);
            }
        }

        // Close file streams
        outputWriter.flushAndClose();
        discaredOutputWriter.flushAndClose();
        genotypeData.close();

        // Log statistics
        LOGGER.info("Done");
    }

    private AlleleSpecificIpcrRecord assignVariantAlleleToIpcrRecord(IpcrRecord ipcrRecord, PairedSamRecord samRecord, GeneticVariant curVariant, int sampleIdx, String source) throws IOException {

        // Determine the variant type
        VariantType variantType = determineVariantType(curVariant);

        // Get the variant alleles as string, could be refactored to Allele objects from GenotypeIO
        // For now this works fine
        String referenceAllele = curVariant.getRefAllele().getAlleleAsString();
        String alternativeAllele = String.join("", curVariant.getAlternativeAlleles().getAllelesAsString());
        int variantPos = curVariant.getStartPos();

        // Get the variant position in the read. SAM positions are 1 based, so are VCFs
        // Given the zero based indexing in java subtract 1. If indel will return the last base
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

        // Determine what the expected allele is based on the genotype if HOM_REF or HOM_ALT. if HET leave as NULL
        // as in this case both alleles are valid.
        String expectedAlleleBasedOnSampleGenotype = null;
        if (sampleIdx >= 0) {
            int sampleDosage = curVariant.getSampleCalledDosages()[sampleIdx];

            if (sampleDosage == 2) {
                expectedAlleleBasedOnSampleGenotype = curVariant.getVariantAlleles().getAllelesAsString().get(0);
            } else if (sampleDosage == 0) {
                expectedAlleleBasedOnSampleGenotype = curVariant.getVariantAlleles().getAllelesAsString().get(1);
            }
        }

        // Switch on the variant type.
        switch (variantType) {
            case SNP:
                // Zero based indexing, hence the -1 above
                tmpReadAllele1 = Character.toString(read.charAt(variantPosInRead));

                // Determine if the allele is REF or ALT
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
                // The REF allele is the 1st base of the position
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
                // Determine the ALT allele
                tmpReadAllele2 = read.substring(variantPosInRead, insertEnd);

                // TODO: verify
                // Cascade to assign the current allele, both REF and ALT need to be checked
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

        AlleleSpecificIpcrRecord outputRecord = new BasicAlleleSpecificIpcrRecord(ipcrRecord, readAllele, altReadAllele, variantPosInRead, curVariant, variantType);
        outputRecord.setSource(samRecord.getSource());

        if (discarded) {
            discaredOutputWriter.writeRecord(outputRecord, source + "\t" + reason);
            return null;
        } else {
            return outputRecord;
        }
    }

    private boolean evaluateIpcrRecord(AlleleSpecificIpcrRecord curAlleleSpecificIpcrRecord, String source, int sampleIdx) throws IOException {

        if (curAlleleSpecificIpcrRecord != null) {
            curAlleleSpecificIpcrRecord.setSampleId(params.getSampleGenotypeId());

            // Check if the read is a artifical haplotype, if so discard
            if (!curAlleleSpecificIpcrRecord.getPrimaryReadName().matches("^HC[0-9]*")) {
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
                        outputWriter.writeRecord(curAlleleSpecificIpcrRecord, source);
                        return true;
                    }
                    if (curAlleleSpecificIpcrRecord.getReadAllele().equals(allele)) {
                        outputWriter.writeRecord(curAlleleSpecificIpcrRecord, source);
                        return true;
                    } else {
                        discaredOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source + "\tReadAlleleMismatchesGenotype:" + curGenotype);
                        return false;
                    }
                } else {
                    outputWriter.writeRecord(curAlleleSpecificIpcrRecord, source);
                    return true;
                }
            } else {
                discaredOutputWriter.writeRecord(curAlleleSpecificIpcrRecord, source + "\tArtificialHaplotype");
            }

        }
        return false;
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

    private RandomAccessGenotypeData readGenotypeData() throws IOException {
        // Probably can decide to only allow VCF here so this method can be simplified
        RandomAccessGenotypeDataReaderFormats format = RandomAccessGenotypeDataReaderFormats.VCF;
        String path = params.getInputVcf();

        RandomAccessGenotypeData gt = null;
        LOGGER.warn("Codebase has only been tested on GATK formatted VCF files, " +
                "although in theory other VCF should work fine your milage may vary");
        switch (format) {
            case GEN:
            case GEN_FOLDER:
            case PED_MAP:
            case PLINK_BED:
            case SHAPEIT2:
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

    private Map<String, IpcrRecord> getOverlappingIpcrRecords(GeneticVariant curVariant, int window) throws IOException {
        // Needs to have a window to account for the local re-alignment by GATK
        Map<String, IpcrRecord> output = ipcrReader.queryAsMap(curVariant.getSequenceName(), curVariant.getStartPos()-window, curVariant.getStartPos() + window);

       // LOGGER.debug(output.size() + " ipcr records overlapping " + curVariant.getPrimaryVariantId());
        return output;
    }

    private Map<String, PairedSamRecord> getOverlappingSamRecords(GeneticVariant curVariant) {

        Map<String, PairedSamRecord> output = new HashMap<>();

        // Get the sam records overlapping the variant
        SAMRecordIterator sammRecordIterator = primarySamReader.queryOverlapping(
                curVariant.getSequenceName(),
                curVariant.getStartPos(),
                curVariant.getStartPos() + 1);

        while (sammRecordIterator.hasNext()) {
            // Retrieve the current record
            SAMRecord record = sammRecordIterator.next();
            String key = record.getReadName().split(" ")[0];

            // Dont use artifical haplotypes from GATK
            if (!key.matches("^HC[0-9]*")) {
                if (output.containsKey(key)) {
                    output.get(key).setTwo(record);
                } else {
                    output.put(key, new PairedSamRecord(record, "PrimarySamReader"));
                }
                //LOGGER.debug(record.getReadName() + "\t" + record.getContig() + "\t" + record.getAlignmentStart() + "\t" + record.getAlignmentEnd());
            }
        }
        sammRecordIterator.close();

        if (secondarySamReader != null) {
            Set<String> primaryReadnameCache = output.keySet();

            // Get the sam records overlapping the variant
            sammRecordIterator = secondarySamReader.queryOverlapping(
                    curVariant.getSequenceName(),
                    curVariant.getStartPos(),
                    curVariant.getStartPos() + 1);

            while (sammRecordIterator.hasNext()) {
                // Retrieve the current record
                SAMRecord record = sammRecordIterator.next();
                String key = record.getReadName().split(" ")[0];

                // Don't save the read if it is already in the output or it is an artifical haplotype
                if (!primaryReadnameCache.contains(key) && !key.matches("^HC[0-9]*")) {
                    if (output.containsKey(key)) {
                        output.get(key).setTwo(record);
                    } else {
                        output.put(key, new PairedSamRecord(record, "SecondarySamReader"));
                    }
                }
            }
            sammRecordIterator.close();
        }

       // LOGGER.debug(output.size() + " bam records overlapping " + curVariant.getPrimaryVariantId());
        return output;
    }

}
