package nl.umcg.suresnp.pipeline.records.ipcrrecord;

import org.molgenis.genotype.variant.GeneticVariant;

public class BasicAlleleSpecificIpcrRecord extends BasicIpcrRecord implements AlleleSpecificIpcrRecord {

    private String readAllele;
    private String alternativeAllele;
    private int variantStartInRead;
    private GeneticVariant geneticVariant;
    private VariantType variantType;
    private String sampleId;
    private String source;

    public BasicAlleleSpecificIpcrRecord(IpcrRecord record, String readAllele, String alternativeAllele, int variantStartInRead, GeneticVariant geneticVariant, VariantType variantType) {
        super(record);
        this.readAllele = readAllele;
        this.alternativeAllele = alternativeAllele;
        this.variantStartInRead = variantStartInRead;
        this.geneticVariant = geneticVariant;
        this.variantType = variantType;
    }

    @Override
    public String getSampleId() {
        return sampleId;
    }

    @Override
    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    @Override
    public String getReadAllele() {
        return readAllele;
    }

    @Override
    public void setReadAllele(String readAllele) {
        this.readAllele = readAllele;
    }

    @Override
    public String getAlternativeAllele() {
        return alternativeAllele;
    }

    @Override
    public void setAlternativeAllele(String alternativeAllele) {
        this.alternativeAllele = alternativeAllele;
    }

    @Override
    public int getVariantStartInRead() {
        return variantStartInRead;
    }

    @Override
    public void setVariantStartInRead(int variantStartInRead) {
        this.variantStartInRead = variantStartInRead;
    }

    @Override
    public GeneticVariant getGeneticVariant() {
        return geneticVariant;
    }

    @Override
    public void setGeneticVariant(GeneticVariant geneticVariant) {
        this.geneticVariant = geneticVariant;
    }

    @Override
    public VariantType getVariantType() {
        return variantType;
    }

    @Override
    public void setVariantType(VariantType variantType) {
        this.variantType = variantType;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }


/*    @Override
    public int getOrientationIndependentStart() {

        // if (getPrimaryStrand() == 0) {
        //    primaryStrand = '+';
        //}
        if (getPrimaryStrand() == '+') {
            return getPrimaryStart();
        } else if (getPrimaryStrand() == '-') {
            int out = getMateStart();
            if (out == 0) {
                return getPrimaryStart();
            }

            return out;
        } else {
            throw new IllegalStateException("Strand must be either + or -");
        }
    }

    @Override
    public int getOrientationIndependentEnd() {
        //if (getPrimaryStrand() == 0) {
        //    primaryStrand = '+';
        // }
        // Minus is correct here
        if (getPrimaryStrand() == '+') {
            int out = getMateEnd();
            if (out == 0) {
                return getPrimaryEnd();
            }
            return out;
        } else if (getPrimaryStrand() == '-') {
            return getPrimaryEnd();
        } else {
            throw new IllegalStateException("Strand must be either + or -");
        }
    }*/

}
