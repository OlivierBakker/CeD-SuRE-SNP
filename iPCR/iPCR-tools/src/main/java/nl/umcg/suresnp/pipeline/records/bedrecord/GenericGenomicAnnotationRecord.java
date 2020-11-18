package nl.umcg.suresnp.pipeline.records.bedrecord;

import java.util.HashMap;
import java.util.Map;

public class GenericGenomicAnnotationRecord extends BedRecord {

    private Map<String, String> annotations;

    public GenericGenomicAnnotationRecord(BedRecord record) {
        super(record.getContig(), record.getStart(), record.getEnd(), record.getScore());
        annotations = new HashMap<>();
        annotations.put("score", String.valueOf(record.getScore()));
    }

    public GenericGenomicAnnotationRecord(NarrowPeakRecord record) {
        super(record);
        annotations = new HashMap<>();
        annotations.put("name", String.valueOf(record.getName()));
        annotations.put("score", String.valueOf(record.getScore()));
        annotations.put("strand", String.valueOf(record.getStrandAsChar()));
        annotations.put("pvalue", String.valueOf(record.getpValue()));
        annotations.put("qvalue", String.valueOf(record.getqValue()));
        annotations.put("signalValue", String.valueOf(record.getSignalValue()));
        annotations.put("peak", String.valueOf(record.getPeak()));
    }

    public GenericGenomicAnnotationRecord(String contig, int start, int end) {
        super(contig, start, end);
        annotations = new HashMap<>();
    }

    public GenericGenomicAnnotationRecord(String contig, int start, int end, double score) {
        super(contig, start, end, score);
        annotations = new HashMap<>();
    }

    public void addAnnotation(String name, String annotation) {
        annotations.put(name, annotation);
    }


    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }
}
