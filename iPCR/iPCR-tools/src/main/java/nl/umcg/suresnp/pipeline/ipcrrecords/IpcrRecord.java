package nl.umcg.suresnp.pipeline.ipcrrecords;

import htsjdk.samtools.SAMRecord;
import org.molgenis.genotype.variant.GeneticVariant;

public class IpcrRecord {

    private String barcode;
    private String readAllele;
    private String alternativeAllele;
    private int variantStartInRead;
    private GeneticVariant geneticVariant;
    private SAMRecord record;
    private VariantType variantType;
    private String SampleId;

    public IpcrRecord(String barcode, String readAllele, String alternativeAllele, int variantStartInRead, GeneticVariant geneticVariant, SAMRecord record, VariantType variantType) {
        this.barcode = barcode;
        this.readAllele = readAllele;
        this.alternativeAllele = alternativeAllele;
        this.variantStartInRead = variantStartInRead;
        this.geneticVariant = geneticVariant;
        this.record = record;
        this.variantType = variantType;
    }

    public String getSampleId() {
        return SampleId;
    }

    public void setSampleId(String sampleId) {
        SampleId = sampleId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getReadAllele() {
        return readAllele;
    }

    public void setReadAllele(String readAllele) {
        this.readAllele = readAllele;
    }

    public String getAlternativeAllele() {
        return alternativeAllele;
    }

    public void setAlternativeAllele(String alternativeAllele) {
        this.alternativeAllele = alternativeAllele;
    }

    public int getVariantStartInRead() {
        return variantStartInRead;
    }

    public void setVariantStartInRead(int variantStartInRead) {
        this.variantStartInRead = variantStartInRead;
    }

    public GeneticVariant getGeneticVariant() {
        return geneticVariant;
    }

    public void setGeneticVariant(GeneticVariant geneticVariant) {
        this.geneticVariant = geneticVariant;
    }

    public SAMRecord getRecord() {
        return record;
    }

    public void setRecord(SAMRecord record) {
        this.record = record;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public void setVariantType(VariantType variantType) {
        this.variantType = variantType;
    }

}
