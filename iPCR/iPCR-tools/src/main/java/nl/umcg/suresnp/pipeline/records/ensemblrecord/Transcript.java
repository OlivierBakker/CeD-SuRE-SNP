package nl.umcg.suresnp.pipeline.records.ensemblrecord;

import htsjdk.samtools.util.Interval;

public class Transcript extends Interval {

    private String transcriptId;

    public Transcript(String chr, int start, int stop, String transcriptId) {
        super(chr, start, stop);
        this.transcriptId = transcriptId;
    }

}
