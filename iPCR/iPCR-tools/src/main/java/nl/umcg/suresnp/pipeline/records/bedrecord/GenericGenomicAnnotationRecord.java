package nl.umcg.suresnp.pipeline.records.bedrecord;

import org.broad.igv.bbfile.BedFeature;

import java.util.*;

public class GenericGenomicAnnotationRecord extends BedRecord {

    private final List<String> annotations;

    public GenericGenomicAnnotationRecord(BedFeature record) {
        super(record.getChromosome(), record.getStartBase(), record.getEndBase(), 0);
        annotations = Arrays.asList(record.getRestOfFields());
        //annotations.put("score", String.valueOf(record.getScore()));
    }

    public GenericGenomicAnnotationRecord(BedRecord record) {
        super(record.getContig(), record.getStart(), record.getEnd(), record.getScore());
        annotations = new ArrayList<>();
        //annotations.put("score", String.valueOf(record.getScore()));
        annotations.add(String.valueOf(record.getScore()));
    }

    public GenericGenomicAnnotationRecord(NarrowPeakRecord record) {
        super(record);
        annotations = new ArrayList<>();
        annotations.add(String.valueOf(record.getName()));
        annotations.add(String.valueOf(record.getScore()));
        annotations.add(String.valueOf(record.getStrandAsChar()));
        annotations.add(String.valueOf(record.getpValue()));
        annotations.add(String.valueOf(record.getqValue()));
        annotations.add(String.valueOf(record.getSignalValue()));
        annotations.add(String.valueOf(record.getPeak()));
    }

    public GenericGenomicAnnotationRecord(String contig, int start, int end) {
        super(contig, start, end);
        this.annotations = new ArrayList<>();
    }

    public GenericGenomicAnnotationRecord(String contig, int start, int end, List<String> annotations) {
        super(contig, start, end);
        this.annotations = annotations;
    }

    public GenericGenomicAnnotationRecord(String contig, int start, int end, String[] annotations) {
        super(contig, start, end);
        this.annotations = Arrays.asList(annotations);
    }

    public GenericGenomicAnnotationRecord(String contig, int start, int end, double score) {
        super(contig, start, end, score);
        this.annotations = new ArrayList<>();
    }

    public void addAnnotation(String annotation) {
        annotations.add(annotation);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenericGenomicAnnotationRecord that = (GenericGenomicAnnotationRecord) o;
        return Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), annotations);
    }
}
