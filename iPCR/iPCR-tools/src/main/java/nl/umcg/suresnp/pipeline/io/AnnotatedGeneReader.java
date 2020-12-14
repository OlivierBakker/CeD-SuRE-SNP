package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.records.ensemblrecord.AnnotatedGene;
import nl.umcg.suresnp.pipeline.records.ensemblrecord.GeneBasedGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.ensemblrecord.Transcript;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedGeneReader {

    private static final Logger LOGGER = Logger.getLogger(AnnotatedGeneReader.class);
    private BufferedReader reader;
    private GenericFile file;
    private static String sep = "\t";
    private String[] header;

    public AnnotatedGeneReader(GenericFile reader, boolean hasHeader) throws IOException {
        this.file = reader;
        this.reader = reader.getAsBufferedReader();

        if (hasHeader) {
            String[] tmp = this.reader.readLine().split(sep);
            this.header = Arrays.copyOfRange(tmp, 12, tmp.length);
        }
    }

    public Map<String, AnnotatedGene> readGenesWithTranscript() throws IOException {
        Map<String, AnnotatedGene> genes = new HashMap<>();
        while (this.reader.ready()) {
            String line = reader.readLine();
            String[] args = line.split(sep);

            AnnotatedGene curAnnotatedGene;
            if (genes.containsKey(args[0])) {
                curAnnotatedGene = genes.get(args[0]);
                curAnnotatedGene.addTranscript(parseAsTranscript(args));
            } else {
                genes.put(args[0], parseAsGene(args));
            }
        }
        return genes;
    }

    public IntervalTreeMap<AnnotatedGene> readGenesWithTranscriptAsIntervalMap() throws IOException {

        IntervalTreeMap<AnnotatedGene> geneIntervalTreeMap = new IntervalTreeMap<>();
        for (AnnotatedGene curAnnotatedGene : readGenesWithTranscript().values()) {
            geneIntervalTreeMap.put(curAnnotatedGene, curAnnotatedGene);
        }

        return geneIntervalTreeMap;
    }

    public GeneBasedGenomicAnnotation readGenesAsGenomicAnnotation() throws IOException {
        return new GeneBasedGenomicAnnotation(file, header, readGenesWithTranscriptAsIntervalMap());
    }

    private Transcript parseAsTranscript(String[] args) {
        return new Transcript(args[9], Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[2]);
    }

    private AnnotatedGene parseAsGene(String[] args) {
        AnnotatedGene curGene = new AnnotatedGene(args[0], // Gene ID
                args[11], // Chromosome
                Integer.parseInt(args[3]), // Start
                Integer.parseInt(args[4]), // End
                args[8], // Band
                args[9], // Gene symbol
                args[10], // Gene type
                Boolean.parseBoolean(args[7]) // Strand
        );

        if (args.length > 12) {
            for (int i = 12; i < args.length; i++) {
                curGene.addAnnotation(args[i]);
            }
        }

        return curGene;
    }

    public String[] getHeader() {
        return header;
    }
}
