package nl.umcg.mafsamplesubset;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class FileWriter {

    private static Logger LOGGER = Logger.getLogger(FileWriter.class);

    public static void writeFile(GenericFile file, List<String> content) throws IOException {
        Files.write(Paths.get(file.getPath()), content);
        LOGGER.info("Contents written to file: " + file.getPath());
    }

    public static void writePermutedSubset(GenericFile file, PermutedSubset subset) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath()), "UTF-8"));

        bw.write("Mean maf of subset: " + subset.getMeanMaf());
        bw.newLine();
        bw.write("Minimum maf of subset: " + subset.getMinimumMaf());
        bw.newLine();
        bw.write("# SNPs passing threshold: " + subset.getTargetCount());
        bw.newLine();
        bw.write("# Selected samples: " + subset.getSamples().length);

        LOGGER.info("Log written to file: " + file.getPath());
        bw.flush();
        bw.close();

    }

    public static void writeAlleleFrequencies(GenericFile file, PermutedSubset subset) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath()), "UTF-8"));
        bw.write( "ID\tA1\tA2\tAfA1\tAfA2\tMAF\tMISSING");
        for (AlleleFrequency freq : subset.getAlleleFrequencies()) {
            bw.newLine();
            bw.write( freq.getVariantId() + "\t" + freq.getAllele1() + "\t" + freq.getAllele2() + "\t" + freq.getAfAllele1() + "\t" + freq.getAfAllele2() + "\t" + freq.getMaf() + "\t" + freq.getMissSnp());
        }

        LOGGER.info("Allele info written to file: " + file.getPath());
        bw.flush();
        bw.close();
    }


    public static void writeSampleList(GenericFile file, PermutedSubset subset) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath()), "UTF-8"));

        for (String sample : subset.getSamples()) {
            bw.write(sample);
            bw.newLine();
        }

        LOGGER.info("Samples written to file: " + file.getPath());
        bw.flush();
        bw.close();
    }

}
