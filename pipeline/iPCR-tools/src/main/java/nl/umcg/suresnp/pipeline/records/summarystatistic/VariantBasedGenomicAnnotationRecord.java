package nl.umcg.suresnp.pipeline.records.summarystatistic;

import java.util.ArrayList;
import java.util.List;

public class VariantBasedGenomicAnnotationRecord extends GeneticVariantInterval {

    //private final List<Double> numericAnnotations;
    private final List<String> textAnnotations;

    public VariantBasedGenomicAnnotationRecord(GeneticVariantInterval variant, List<String> textAnnotations) {
        super(variant);
        this.textAnnotations = textAnnotations;
    }

/*
    public VariantBasedGenomicAnnotationRecord(GeneticVariantInterval variant) {
        super(variant);
        this.numericAnnotations = new ArrayList<>();
        this.textAnnotations = new ArrayList<>();
    }

    public VariantBasedGenomicAnnotationRecord(GeneticVariantInterval variant, List<Double> numericAnnotations) {
        super(variant);
        this.numericAnnotations = numericAnnotations;
        this.textAnnotations = new ArrayList<>();
    }

    public VariantBasedGenomicAnnotationRecord(GeneticVariantInterval variant, List<Double> numericAnnotations, List<String> textAnnotations) {
        super(variant);
        this.numericAnnotations = numericAnnotations;
        this.textAnnotations = textAnnotations;
    }
*/

    public void addTextAnnotation(String annotation) {this.textAnnotations.add(annotation);}

/*
    public void addNumericAnnotation(Double annotation) {
        this.numericAnnotations.add(annotation);
    }
*/

/*    public List<Double> getNumericAnnotations() {
        return numericAnnotations;
    }*/

    public List<String> getTextAnnotations() {
        return textAnnotations;
    }
}
