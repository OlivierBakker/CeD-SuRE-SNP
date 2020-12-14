package nl.umcg.suresnp.pipeline.records.ensemblrecord;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public class AnnotatedGene extends Interval implements Feature, Locatable {

    private String gene;
    private String geneSymbol;
    private String band;
    private String geneType;
    private boolean strand;
    private IntervalTreeMap<Transcript> transcripts;
    private List<String> annotations;

    public AnnotatedGene(String gene, String chr, int start, int stop, String band, String geneSymbol, String geneType) {
        super(chr.intern(), start, stop);
        this.gene = gene;
        this.band = band;
        this.geneSymbol = geneSymbol;
        this.strand = true;
        this.geneType = geneType;
        this.transcripts = new IntervalTreeMap<>();
        this.annotations = new ArrayList<>();
    }

    public AnnotatedGene(String gene, String chr, int start, int stop, String band, String geneSymbol, String geneType, boolean strand) {
        super(chr.intern(), start, stop);
        this.gene = gene;
        this.band = band;
        this.geneSymbol = geneSymbol;
        this.strand = strand;
        this.geneType = geneType;
        this.transcripts = new IntervalTreeMap<>();
        this.annotations = new ArrayList<>();
    }

    public int getTssPos() {
        if (strand) {
            return getStart();
        } else {
            return getEnd();
        }
    }

    public Collection<Transcript> overlappingTranscripts(Locatable key) {
        return transcripts.getOverlapping(key);
    }

    public void addTranscript(Transcript transcript) {
        transcripts.put(transcript, transcript);
    }

    public OverlapType determineOverlapType(Locatable key) {
        if (this.overlappingTranscripts(key).size() > 0) {
            return OverlapType.EXON;
        } else if (this.overlaps(key) && this.transcripts.size() > 0) {
            return OverlapType.INTRON;
        } else if (this.overlaps(key)){
            return OverlapType.GENE_BODY;
        }
        return OverlapType.EXTERNAL;
    }

    public String getGene() {
        return gene;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getBand() {
        return band;
    }

    public String getChrAndArm() {
        StringBuilder sb = new StringBuilder(getContig());
        if (!this.band.equals("")) {
            sb.append('_');
            sb.append(band.charAt(0));
        }
        return sb.toString().intern();

    }

    public String getGeneType() {
        return geneType;
    }

    public boolean isStrand() {
        return strand;
    }

    public IntervalTreeMap<Transcript> getTranscripts() {
        return transcripts;
    }

    public int getLength() {
        return getEnd() - getStart();
    }

    public void addAnnotation(String annotation) {
        annotations.add(annotation);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnnotatedGene other = (AnnotatedGene) obj;
        if (this.getStart() != other.getStart()) {
            return false;
        }
        if (this.getEnd() != other.getEnd()) {
            return false;
        }
        if (!Objects.equals(this.gene, other.gene)) {
            return false;
        }
        if (!Objects.equals(this.getContig(), other.getContig())) {
            return false;
        }
        if (!Objects.equals(this.band, other.band)) {
            return false;
        }
        return true;
    }

}
