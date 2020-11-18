package nl.umcg.suresnp.pipeline.records.bedrecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericGenomicAnnotationRecord extends BedRecord {

    private List<String> annotations;

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
        annotations = new ArrayList<>();
    }

    public GenericGenomicAnnotationRecord(String contig, int start, int end, double score) {
        super(contig, start, end, score);
        annotations = new ArrayList<>();
    }

    public void addAnnotation(String annotation) {
        annotations.add(annotation);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
}
