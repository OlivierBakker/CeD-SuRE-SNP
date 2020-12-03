package nl.umcg.suresnp.pipeline.records.ensemblrecord;

public class Transcript extends Gene {

    private String transcriptId;

    public Transcript(String gene, String chr, int start, int stop, String band, String geneSymbol, boolean strand, String transcriptId) {
        super(gene, chr, start, stop, band, geneSymbol, strand);
        this.transcriptId = transcriptId;
    }

}
