package nl.umcg.suresnp.pipeline.records.summarystatistic;

import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.util.Map;

public class VariantBasedGenomicAnnotation {

    private final GenericFile path;
    private String[] header;
    private String name;
    private String group;
    private boolean useLdProxies;
    private Map<String, VariantBasedGenomicAnnotationRecord> records;

    public VariantBasedGenomicAnnotation(GenericFile path, String group) {
        this.path = path;
        this.group = group;
        this.name = path.getBaseName();
        if (group.startsWith("UseLd")) {
            useLdProxies = true;
            this.group = group.replace("UseLd", "");
        } else {
            useLdProxies = false;
        }
    }

    public VariantBasedGenomicAnnotation(GenericFile path, String[] header, Map<String, VariantBasedGenomicAnnotationRecord> records) {
        this.path = path;
        this.records = records;
        this.header = header;
        this.name = path.getBaseName();
        this.useLdProxies=false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    public String getGroup() {
        return group;
    }

    public boolean useLdProxies() {
        return useLdProxies;
    }

    public Map<String, VariantBasedGenomicAnnotationRecord> getRecords() {
        return records;
    }

    public void addRecord(VariantBasedGenomicAnnotationRecord record) {
        records.put(record.getPrimaryVariantId(), record);
    }

    public void setRecords(Map<String, VariantBasedGenomicAnnotationRecord> records) {
        this.records = records;
    }

    public GenericFile getPath() {
        return path;
    }

    public String[] getHeader() {
        return header;
    }

    public VariantBasedGenomicAnnotationRecord query(String variantId) {
        return records.get(variantId);
    }
}
