package nl.umcg.suresnp.pipeline.records.bedrecord;

import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.Locatable;
import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.util.Collection;
import java.util.List;

public class GenericGenomicAnnotation {

    private GenericFile path;
    private String name;
    private String[] header;
    private IntervalTreeMap<GenericGenomicAnnotationRecord> records;


    public GenericGenomicAnnotation(GenericFile path, String[] header, IntervalTreeMap<GenericGenomicAnnotationRecord> records) {
        this.path = path;
        this.records = records;
        this.header = header;
        this.name = path.getBaseName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IntervalTreeMap<GenericGenomicAnnotationRecord> getRecords() {
        return records;
    }

    public void setRecords(IntervalTreeMap<GenericGenomicAnnotationRecord> records) {
        this.records = records;
    }

    public GenericFile getPath() {
        return path;
    }

    public String[] getHeader() {
        return header;
    }


    public Collection<GenericGenomicAnnotationRecord> query(Locatable key) {
        return records.getOverlapping(key);
    }
}
