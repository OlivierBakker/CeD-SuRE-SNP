package nl.umcg.suresnp.pipeline.records.ensemblrecord;

import htsjdk.samtools.util.Locatable;
import htsjdk.tribble.Feature;

import java.util.Objects;


/**
 * Ported from systemgenetics/downstreamer, refactored to use Locatable
 *
 */
public class Gene implements Feature, Locatable {

    private final String gene;
    private final String geneSymbol;
    private final String chr;
    private final int start;
    private final int end;
    private final String band;

    public Gene(String gene, String chr, int start, int stop, String band) {
        this.gene = gene;
        this.chr = chr.intern();
        this.start = start;
        this.end = stop;
        this.band = band;
        this.geneSymbol = null;
    }

    public Gene(String gene, String chr, int start, int stop, String band, String geneSymbol) {
        this.gene = gene;
        this.chr = chr.intern();
        this.start = start;
        this.end = stop;
        this.band = band;
        this.geneSymbol = geneSymbol;
    }

    public String getGene() {
        return gene;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getChr() {
        return chr;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public String getContig() {
        return getChr();
    }

    public String getBand() {
        return band;
    }

    public String getChrAndArm() {

        StringBuilder sb = new StringBuilder(chr);
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
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        if (!Objects.equals(this.gene, other.gene)) {
            return false;
        }
        if (!Objects.equals(this.chr, other.chr)) {
            return false;
        }
        if (!Objects.equals(this.band, other.band)) {
            return false;
        }
        return true;
    }

}
