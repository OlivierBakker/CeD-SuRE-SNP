package nl.umcg.suresnp.pipeline.records.summarystatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VariantBasedNumericGenomicAnnotationRecord extends GeneticVariant {

    private final List<Double> annotations;

    public VariantBasedNumericGenomicAnnotationRecord(GeneticVariant variant) {
        super(variant);
        this.annotations = new ArrayList<>();
    }

    public VariantBasedNumericGenomicAnnotationRecord(GeneticVariant variant, List<Double> annotations) {
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
