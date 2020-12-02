package nl.umcg.suresnp.pipeline.records.summarystatistic;

import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.Locatable;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class VariantBasedNumericGenomicAnnotation {

    private final GenericFile path;
    private final String[] header;
    private String name;
    private Map<String, VariantBasedNumericGenomicAnnotationRecord> records;

    public VariantBasedNumericGenomicAnnotation(GenericFile path, String[] header, Map<String, VariantBasedNumericGenomicAnnotationRecord> records) {
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

    public Map<String, VariantBasedNumericGenomicAnnotationRecord> getRecords() {
        return records;
    }

    public void addRecord(VariantBasedNumericGenomicAnnotationRecord record) {
        records.put(record.getPrimaryVariantId(), record);
    }

    public void setRecords(Map<String, VariantBasedNumericGenomicAnnotationRecord> records) {
        this.records = records;
    }

    public GenericFile getPath() {
        return path;
    }

    public String[] getHeader() {
        return header;
    }

    public VariantBasedNumericGenomicAnnotationRecord query(String variantId) {
        return records.get(variantId);
    }
}
