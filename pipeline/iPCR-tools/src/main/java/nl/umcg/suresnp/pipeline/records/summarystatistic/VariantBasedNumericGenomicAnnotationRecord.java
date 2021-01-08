package nl.umcg.suresnp.pipeline.records.summarystatistic;

import java.util.ArrayList;
import java.util.List;

public class VariantBasedNumericGenomicAnnotationRecord extends GeneticVariantInterval {

    private final List<Double> annotations;

    public VariantBasedNumericGenomicAnnotationRecord(GeneticVariantInterval variant) {
        super(variant);
        this.annotations = new ArrayList<>();
    }

    public VariantBasedNumericGenomicAnnotationRecord(GeneticVariantInterval variant, List<Double> annotations) {
        super(variant);
        this.annotations = annotations;
    }

    public void addAnnotation(Double annotation) {
        this.annotations.add(annotation);
    }

    public List<Double> getAnnotations() {
        return annotations;
    }
}
