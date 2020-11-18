package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.records.ensemblrecord.Gene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IoUtils {

    public static List<Gene> readGenes(GenericFile geneFile) throws IOException {

        BufferedReader reader = geneFile.getAsBufferedReader();
        final ArrayList<Gene> genes = new ArrayList<>();

        String nextLine;
        int i = 0;
        while ((nextLine = reader.readLine()) != null) {

            String[] fields = nextLine.split("\t");
            if (i > 0) {
                genes.add(new Gene(fields[0], fields[1], Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), fields[5], fields[6]));
            }
            i++;
        }

        return genes;
    }
}
