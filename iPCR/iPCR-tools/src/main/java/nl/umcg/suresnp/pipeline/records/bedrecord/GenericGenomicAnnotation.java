package nl.umcg.suresnp.pipeline.records.bedrecord;

import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.Locatable;
import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GenericGenomicAnnotation {

    private final GenericFile path;
    private final String[] header;
    private String name;
    private IntervalTreeMap<Set<GenericGenomicAnnotationRecord>> records;

    public GenericGenomicAnnotation(GenericFile path, String[] header, IntervalTreeMap<Set<GenericGenomicAnnotationRecord>> records) {
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

    public IntervalTreeMap<Set<GenericGenomicAnnotationRecord>> getRecords() {
        return records;
    }

    public void setRecords(IntervalTreeMap<Set<GenericGenomicAnnotationRecord>> records) {
        this.records = records;
    }

    public GenericFile getPath() {
        return path;
    }

    public String[] getHeader() {
        return header;
    }

    public Collection<GenericGenomicAnnotationRecord> query(Locatable key) {
        List<GenericGenomicAnnotationRecord> tmp = new ArrayList<>();
        for( Collection<GenericGenomicAnnotationRecord> cur : records.getOverlapping(key)) {
            tmp.addAll(cur);
        };

        return tmp;
    }
}
