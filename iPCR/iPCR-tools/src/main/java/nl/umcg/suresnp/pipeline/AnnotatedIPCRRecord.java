package nl.umcg.suresnp.pipeline;

import htsjdk.samtools.SAMRecord;
import org.molgenis.genotype.variant.GeneticVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotatedIPCRRecord extends IPCRRecord {

    private List<GeneticVariant> overlappingVariants;

    public AnnotatedIPCRRecord(String barcode, SAMRecord cachedSamRecord, SAMRecord mate) {
        super(barcode, cachedSamRecord, mate);
        this.overlappingVariants = new ArrayList<>();
    }

    public AnnotatedIPCRRecord(String barcode, String referenceSequence, int startOne, int endOne, int startTwo, int endTwo, char orientation, int mapqOne, int mapqTwo, String cigarOne, String cigarTwo, String sequenceOne, String sequenceTwo, int duplicateCount) {
        super(barcode, referenceSequence, startOne, endOne, startTwo, endTwo, orientation, mapqOne, mapqTwo, cigarOne, cigarTwo, sequenceOne, sequenceTwo, duplicateCount);
        this.overlappingVariants = new ArrayList<>();
    }


    public boolean posistionInMappedRegion(int begin, int end) {
        if (begin > this.getStartOne() && begin < this.getEndOne() && end > this.getStartOne() && end < this.getEndOne()) {
            return true;
        }

        if (begin > this.getStartTwo() && begin < this.getEndTwo() && end > this.getStartTwo() && end < this.getEndTwo()) {
            return true;
        }

        return false;
    }


    public void addGeneticVariant(GeneticVariant variant) {
        overlappingVariants.add(variant);
    }

    public String getBaseAt(int begin, int end) {

        try {
            if (begin > this.getStartOne() && begin < this.getEndOne()) {
                return getSequenceOne().substring(begin - getStartOne(), (end - getStartOne()));
            }

            if (begin > this.getStartTwo() && begin < this.getEndTwo()) {
                return getSequenceTwo().substring(begin - getStartTwo(), (end - getStartTwo()));
            }
        } catch (StringIndexOutOfBoundsException e) {
            System.out.print("");
            return null;
        }
        return null;
    }

    @Override
    public String getOutputString(String sep) {
        String base = super.getOutputString(sep);
        StringBuilder sb = new StringBuilder();
        sb.append(base);

        StringBuilder varIds = new StringBuilder();
        StringBuilder rsIds = new StringBuilder();
        StringBuilder alleles = new StringBuilder();

        int i = 0;
        for (GeneticVariant variant : overlappingVariants) {

            // Add one because the BAM files are 1 indexed and the VCF zero indexed
            String varId = variant.getSequenceName() + ":" + variant.getStartPos() + ":" + variant.getRefAllele().toString() + ":" + String.join("|", variant.getAlternativeAlleles().getAllelesAsString());
            String rsId = variant.getPrimaryVariantId();
            String allele = this.getBaseAt(variant.getStartPos(), variant.getStartPos() + variant.getAlternativeAlleles().getAllelesAsString().get(0).length());
            //String position = Integer.toString(variant.getStartPos());

            if (i == 0) {
                varIds.append(varId);
                rsIds.append(rsId);
                alleles.append(allele);
               // positions.append(position);
            } else {
                varIds.append(",").append(varId);
                rsIds.append(",").append(rsId);
                alleles.append(",").append(allele);
               // positions.append(",").append(position);
            }
            i++;
        }

        sb.append(sep)
                .append(varIds)
                .append(sep)
                .append(rsIds)
                .append(sep)
                .append(alleles);
                //.append(sep)
                //.append(positions);


        return sb.toString();
    }



}
