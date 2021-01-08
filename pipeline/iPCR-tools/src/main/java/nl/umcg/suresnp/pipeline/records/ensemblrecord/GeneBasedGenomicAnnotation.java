package nl.umcg.suresnp.pipeline.records.ensemblrecord;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.Locatable;
import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.util.Collection;

public class GeneBasedGenomicAnnotation {

    private GenericFile file;
    private String[] header;
    private IntervalTreeMap<AnnotatedGene> records;

    public GeneBasedGenomicAnnotation(GenericFile file, String[] header, IntervalTreeMap<AnnotatedGene> records) {
        this.file = file;
        this.header = header;
        this.records = records;
    }

    public String[] getHeader() {
        return header;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    public IntervalTreeMap<AnnotatedGene> getRecords() {
        return records;
    }

    public void setRecords(IntervalTreeMap<AnnotatedGene> records) {
        this.records = records;
    }

    public Collection<AnnotatedGene> queryOverlapping(Locatable key) {
        return records.getOverlapping(new Interval(key.getContig(), key.getStart(), key.getEnd()+1));
    }

    public AnnotatedGene queryClosestTssWindow(Locatable key, int window) {
        int indexPos = (key.getStart() + key.getEnd()) / 2;

        AnnotatedGene closest = null;
        for (AnnotatedGene curGene : this.records.getOverlapping(new Interval(key.getContig(), key.getStart() - window, key.getEnd() + window))) {
            if (closest == null) {
                closest  = curGene;
            } else if (Math.abs(closest.getTssPos() - indexPos) > Math.abs(curGene.getTssPos() - indexPos)) {
                closest = curGene;
            }
        }

        return closest;
    }
}
