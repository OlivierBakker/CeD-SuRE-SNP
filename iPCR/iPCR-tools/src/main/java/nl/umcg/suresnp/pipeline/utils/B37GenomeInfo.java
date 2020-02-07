package nl.umcg.suresnp.pipeline.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class B37GenomeInfo {
    private static final Map<String, Integer> chromSizes;
    private static final List<String> chromosomes;

    static {
        chromosomes = new ArrayList<>() {{
            add("1");
            add("2");
            add("3");
            add("4");
            add("5");
            add("6");
            add("7");
            add("8");
            add("9");
            add("10");
            add("11");
            add("12");
            add("13");
            add("14");
            add("15");
            add("16");
            add("17");
            add("18");
            add("19");
            add("20");
            add("21");
            add("22");
            add("x");
            add("y");
            add("X");
            add("Y");
            add("chr1");
            add("chr2");
            add("chr3");
            add("chr4");
            add("chr5");
            add("chr6");
            add("chr7");
            add("chr8");
            add("chr9");
            add("chr10");
            add("chr11");
            add("chr12");
            add("chr13");
            add("chr14");
            add("chr15");
            add("chr16");
            add("chr17");
            add("chr18");
            add("chr19");
            add("chr20");
            add("chr21");
            add("chr22");
            add("chrx");
            add("chry");
            add("chrX");
            add("chrY");
        }};

        chromSizes = new HashMap<>() {{
            put("chr1", 249250621);
            put("chr2", 243199373);
            put("chr3", 198022430);
            put("chr4", 191154276);
            put("chr5", 180915260);
            put("chr6", 171115067);
            put("chr7", 159138663);
            put("chrX", 155270560);
            put("chr8", 146364022);
            put("chr9", 141213431);
            put("chr10", 135534747);
            put("chr11", 135006516);
            put("chr12", 133851895);
            put("chr13", 115169878);
            put("chr14", 107349540);
            put("chr15", 102531392);
            put("chr16", 90354753);
            put("chr17", 81195210);
            put("chr18", 78077248);
            put("chr20", 63025520);
            put("chrY", 59373566);
            put("chr19", 59128983);
            put("chr22", 51304566);
            put("chr21", 48129895);
            put("1", 249250621);
            put("2", 243199373);
            put("3", 198022430);
            put("4", 191154276);
            put("5", 180915260);
            put("6", 171115067);
            put("7", 159138663);
            put("X", 155270560);
            put("8", 146364022);
            put("9", 141213431);
            put("10", 135534747);
            put("11", 135006516);
            put("12", 133851895);
            put("13", 115169878);
            put("14", 107349540);
            put("15", 102531392);
            put("16", 90354753);
            put("17", 81195210);
            put("18", 78077248);
            put("20", 63025520);
            put("Y", 59373566);
            put("19", 59128983);
            put("22", 51304566);
            put("21", 48129895);
        }};

    }

    public static Map<String, Integer> getChromSizes() {
        return chromSizes;
    }

    public static List<String> getChromosomes() {
        return chromosomes;
    }

    public static int getChromSize(String chrom) {
        return chromSizes.get(chrom);
    }

    public static boolean isChromosome(String contig) {
        return (chromosomes.contains(contig));
    }

}
