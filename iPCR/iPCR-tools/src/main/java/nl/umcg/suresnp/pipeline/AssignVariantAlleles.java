package nl.umcg.suresnp.pipeline;


import htsjdk.samtools.*;
import nl.umcg.suresnp.pipeline.barcodes.filters.AdapterSequenceMaxMismatchFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.FivePrimeFragmentLengthEqualsFilter;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.*;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.BarcodeFileReader;
import nl.umcg.suresnp.pipeline.io.barcodefilereader.GenericBarcodeFileReader;
import nl.umcg.suresnp.pipeline.io.icpr.DiscaredIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrParseException;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.VariantType;
import org.apache.log4j.Logger;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.umcg.suresnp.pipeline.ipcrrecords.VariantType.INSERTION;

public class AssignVariantAlleles {

    private static final Logger LOGGER = Logger.getLogger(AssignVariantAlleles.class);
    private IpcrOutputWriter ipcrOutputWriter;
    private DiscaredIpcrRecordWriter discaredOutputWriter;
    private BarcodeFileReader barcodeFileReader;

    public AssignVariantAlleles(IpcrToolsParameters params) throws IOException {

        this.barcodeFileReader = new GenericBarcodeFileReader(params.getOutputPrefix());
        //this.ipcrOutputWriter = new GenericIpcrRecordWriter(new File(params.getOutputPrefix() + ".ipcr"), false);
        this.ipcrOutputWriter = params.getOutputWriter();
        this.discaredOutputWriter = new DiscaredIpcrRecordWriter(new File(params.getOutputPrefix() + ".discarded.reads.txt"), false);
    }

    public void run(IpcrToolsParameters params) throws IOException, IpcrParseException {

        String inputGenotype = params.getInputVcf();

        // Read barcode data
        List<InfoRecordFilter> filters = new ArrayList<>();
        filters.add(new FivePrimeFragmentLengthEqualsFilter(params.getBarcodeLength()));
        filters.add(new AdapterSequenceMaxMismatchFilter(params.getAdapterMaxMismatch()));
       // Map<String, InfoRecord> barcodes = barcodeFileReader.readBarcodeFile(new GenericFile(params.getInputBarcodes()), filters);
        Map<String, String> barcodes = barcodeFileReader.readBarcodeFileAsStringMap(new GenericFile(params.getInputBarcodes()), filters);

        // Create genotype data iterator
        RandomAccessGenotypeData genotypeData = readGenotypeData(RandomAccessGenotypeDataReaderFormats.VCF, params.getInputVcf());

        // Create SAM reader
        SamReader samReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File(params.getInputBam()));

        int i = 0;
        for (GeneticVariant curVariant : genotypeData) {

            if (!curVariant.isBiallelic()) {
                LOGGER.warn("Skipping " + curVariant.getPrimaryVariantId() + " variant is not bi-allelic");
                continue;
            }

            // Get variant information
            VariantType variantType = determineVariantType(curVariant);

            // Get the sam records overlapping the variant
            SAMRecordIterator curSamRecordIterator = samReader.queryOverlapping(curVariant.getSequenceName(), curVariant.getStartPos(), curVariant.getStartPos() + 1);

            // Loop over all the records
            while (curSamRecordIterator.hasNext()) {
                // Logging progress
                if (i > 0) {
                    if (i % 1000000 == 0) {
                        LOGGER.info("Processed " + i / 1000000 + " million SAM records");
                    }
                }

                // Retrieve the current record
                SAMRecord record = curSamRecordIterator.next();
                IpcrRecord curIpcrRecord = assignVariantAlleleToSamRecord(record, variantType, curVariant);
                if (curIpcrRecord != null) {
                    String curBarcode = barcodes.get(record.getReadName());
                    if (curBarcode == null) {
                        discaredOutputWriter.writeRecord(curIpcrRecord, "BarcodeNotAvail");
                    } else {
                        curIpcrRecord.setBarcode(curBarcode);
                        ipcrOutputWriter.writeRecord(curIpcrRecord);
                    }

                }

                i++;
            }


            curSamRecordIterator.close();
        }

        // Close file streams
        ipcrOutputWriter.close();
        barcodeFileReader.close();
        genotypeData.close();

        // Log statistics
        LOGGER.info("Done");
    }

    private IpcrRecord assignVariantAlleleToSamRecord(SAMRecord samRecord, VariantType variantType, GeneticVariant curVariant) throws IOException {
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

                if (tmpReadAllele1.equals(referenceAllele) && !tmpReadAllele2.equals(alternativeAllele)) {
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

                if (tmpReadAllele1.equals(referenceAllele)) {
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

        IpcrRecord outputRecord = new IpcrRecord(null, readAllele, altReadAllele, variantPosInRead, curVariant, samRecord, variantType);

        if (discarded) {
            discaredOutputWriter.writeRecord(outputRecord, reason);
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
