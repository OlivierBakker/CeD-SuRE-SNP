package nl.umcg.suresnp.pipeline.records.ensemblrecord;

import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;

import java.util.Objects;


/**
 * Ported from systemgenetics/downstreamer, refactored to use Locatable
 *
 */
public class Gene extends Interval implements Feature, Locatable {

    private final String gene;
    private final String geneSymbol;
    private final String band;
    private final boolean strand;


    public Gene(String gene, String chr, int start, int stop, String band, String geneSymbol) {
        super(chr.intern(), start, stop);
        this.gene = gene;
        this.band = band;
        this.geneSymbol = geneSymbol;
        this.strand = true;
    }

    public Gene(String gene, String chr, int start, int stop, String band, String geneSymbol, boolean strand) {
        super(chr.intern(), start, stop);
        this.gene = gene;
        this.band = band;
        this.geneSymbol = geneSymbol;
        this.strand = strand;
    }

    public int getTssPos() {
        if (strand) {
            return getStart();
        } else {
            return getEnd();
        }
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

    public int getLength() {
        return getEnd() - getStart();
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
        final Gene other = (Gene) obj;
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
