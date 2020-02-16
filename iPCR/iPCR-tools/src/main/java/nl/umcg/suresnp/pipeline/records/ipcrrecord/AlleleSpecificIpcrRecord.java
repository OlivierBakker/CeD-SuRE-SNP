package nl.umcg.suresnp.pipeline.records.ipcrrecord;

import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import org.molgenis.genotype.variant.GeneticVariant;

public interface AlleleSpecificIpcrRecord extends IpcrRecord {

    String getSampleId();

    void setSampleId(String sampleId);

    String getReadAllele();

    void setReadAllele(String readAllele);

    String getAlternativeAllele();

    void setAlternativeAllele(String alternativeAllele);

    int getVariantStartInRead();

    void setVariantStartInRead(int variantStartInRead);

    GeneticVariant getGeneticVariant();

    void setGeneticVariant(GeneticVariant geneticVariant);

    VariantType getVariantType();

    void setVariantType(VariantType variantType);

    String getSource();

    void setSource(String source);


}
