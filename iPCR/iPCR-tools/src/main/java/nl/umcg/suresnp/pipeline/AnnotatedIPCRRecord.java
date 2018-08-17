package nl.umcg.suresnp.pipeline;

import htsjdk.samtools.SAMRecord;
import org.molgenis.genotype.variant.GeneticVariant;

import java.util.ArrayList;
import java.util.List;

public class AnnotatedIPCRRecord extends IPCRRecord {

    private List<GeneticVariant> overlappingVariants;
    private int invalidVariantAlleles;
    private int undeterminedVariantAlleles;

    public AnnotatedIPCRRecord(String barcode, SAMRecord cachedSamRecord, SAMRecord mate) {
        super(barcode, cachedSamRecord, mate);
        this.overlappingVariants = new ArrayList<>();
        this.invalidVariantAlleles = 0;
        this.undeterminedVariantAlleles = 0;
    }

    public AnnotatedIPCRRecord(String barcode, String referenceSequence, int startOne, int endOne, int startTwo, int endTwo, char orientation, int mapqOne, int mapqTwo, String cigarOne, String cigarTwo, String sequenceOne, String sequenceTwo, int duplicateCount) {
        super(barcode, referenceSequence, startOne, endOne, startTwo, endTwo, orientation, mapqOne, mapqTwo, cigarOne, cigarTwo, sequenceOne, sequenceTwo, duplicateCount);
        this.overlappingVariants = new ArrayList<>();
        this.invalidVariantAlleles = 0;
        this.undeterminedVariantAlleles = 0;
    }

    @Override
    public String getOutputString(String sep) {
        // Only works with bi-allelic variants, otherwise it will take the first allele.
        // This assumes that the alignments are correct and that the reads support the variants.
        // Reads where variants do not match to either alt or ref are omitted in the output.
        // FP can happen if by chance the base at the variant position happens to match to either ref or alt.
        // However stringent QC on the alignments and variant calling should eliminate this.
        // Given the nature of the experiment and the large sequencing depth, even if some FP occur it should
        // not influence the results to a large extent.
        String base = super.getOutputString(sep);
        StringBuilder sb = new StringBuilder();
        sb.append(base);

        StringBuilder varIds = new StringBuilder();
        StringBuilder rsIds = new StringBuilder();
        StringBuilder alleles = new StringBuilder();

        int i = 0;
        for (GeneticVariant variant : overlappingVariants) {

            // Determine the length of ref and alt alleles, used for determining insert vs deletion.
            // This works only for bi-allelic variants, if multi allelic the first is taken.
            int refLength = variant.getRefAllele().toString().length();
            int altLength = variant.getAlternativeAlleles().getAllelesAsString().get(0).length();
            String allele = this.getBaseAt(variant.getStartPos(), variant.getStartPos() + ((altLength + refLength) - 1));
            VariantType variantType = checkGeneticVariantAlleles(variant);

            String varId = variant.getSequenceName() + ":" + variant.getStartPos() + ":" + variant.getRefAllele().toString() + ":" + String.join("|", variant.getAlternativeAlleles().getAllelesAsString());
            String rsId = variant.getPrimaryVariantId();

            switch (variantType) {
                case SNP:
                    break;
                case INSERTION:
                    // Insertion, determine if its the inserted or reference allele
                    if (!allele.equals(variant.getAlternativeAlleles().getAllelesAsString().get(0))) {
                        allele = allele.substring(0, 1);
                    }
                    break;
                case DELETION:
                    // Deletion
                    if (!allele.equals(variant.getRefAllele().toString())) {
                        allele = allele.substring(0, 1);
                    }
                    break;
                case INVALID:
                    // These should have been filtered out already, but it doesn't hurt to check
                    continue;
                case UNDETERMINED:
                    // These should have been filtered out already, but it doesn't hurt to check
                    continue;
            }

            if (i == 0) {
                varIds.append(varId);
                rsIds.append(rsId);
                alleles.append(allele);
            } else {
                varIds.append(",").append(varId);
                rsIds.append(",").append(rsId);
                alleles.append(",").append(allele);
            }
            i++;
        }

        sb.append(sep)
                .append(varIds)
                .append(sep)
                .append(rsIds)
                .append(sep)
                .append(alleles);

        return sb.toString();
    }

    public int getInvalidVariantAlleles() {
        return invalidVariantAlleles;
    }

    public int getUndeterminedVariantAlleles() {
        return undeterminedVariantAlleles;
    }

    public int getValidVariantAlleles() {
        return overlappingVariants.size();
    }

    public void addGeneticVariant(GeneticVariant variant) {
        VariantType variantType = checkGeneticVariantAlleles(variant);
        switch (variantType) {
            case INVALID:
                invalidVariantAlleles++;
                break;
            case UNDETERMINED:
                undeterminedVariantAlleles++;
                break;
            case SNP:
            case INSERTION:
            case DELETION:
            default:
                overlappingVariants.add(variant);
        }
    }

    public boolean positionInMappedRegion(int begin, int end) {
        if (begin > this.getStartOne() && begin < this.getEndOne() && end > this.getStartOne() && end < this.getEndOne()) {
            return true;
        }

        if (begin > this.getStartTwo() && begin < this.getEndTwo() && end > this.getStartTwo() && end < this.getEndTwo()) {
            return true;
        }

        return false;
    }

    private String getBaseAt(int begin, int end) {

        try {
            if (begin > this.getStartOne() && begin < this.getEndOne()) {
                return getSequenceOne().substring(begin - getStartOne(), (end - getStartOne()));
            }

            if (begin > this.getStartTwo() && begin < this.getEndTwo()) {
                return getSequenceTwo().substring(begin - getStartTwo(), (end - getStartTwo()));
            }
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
        return null;
    }

    private VariantType checkGeneticVariantAlleles(GeneticVariant variant) {

        // Determine the length of ref and alt alleles, used for determining insert vs deletion.
        // This works only for bi-allelic variants, if multi allelic the first is taken.
        int refLength = variant.getRefAllele().toString().length();
        int altLength = variant.getAlternativeAlleles().getAllelesAsString().get(0).length();
        String allele = this.getBaseAt(variant.getStartPos(), variant.getStartPos() + ((altLength + refLength) - 1));
        VariantType returnType;

        if (allele != null) {
            // Determine the allele of the read
            if (refLength == 1 && altLength == 1) {
                returnType = VariantType.SNP;
            } else if (refLength == 1 && altLength > 1) {
                returnType = VariantType.INSERTION;
            } else if (refLength > 1 && altLength == 1) {
                returnType = VariantType.DELETION;
            } else {
                returnType = VariantType.INVALID;
            }

            // Check if the allele matches to either ref or alt. If it doesn't the read does not support the variant
            // and is declared invalid.
            if (allele.equals(variant.getRefAllele().toString()) || allele.equals(variant.getAlternativeAlleles().getAllelesAsString().get(0))) {
                // Do nothing if valid
            } else {
                returnType = VariantType.INVALID;
            }

        } else {
            // TODO: depending on the real data fix this issue by making a better extraction for the sub sequence
            // If there is an error in extracting the submergence the variant is undetermined.
            // This can happen if a insertion is at the end of a fragment and the fragment
            // overlaps the insertion but it does not fully contain it or it contains the ref allele
            returnType = VariantType.UNDETERMINED;
        }

        return returnType;
    }
}
