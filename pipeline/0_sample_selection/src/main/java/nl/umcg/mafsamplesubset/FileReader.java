package nl.umcg.mafsamplesubset;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.molgenis.genotype.GenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.plink.BedBimFamGenotypeData;
import org.molgenis.genotype.variantFilter.VariantFilter;
import org.molgenis.genotype.variantFilter.VariantFilterableGenotypeDataDecorator;
import org.molgenis.genotype.variantFilter.VariantIdIncludeFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by olivier on 07/12/2017.
 */
public final class FileReader {

    private static Logger LOGGER = Logger.getLogger(FileReader.class);

    private FileReader() {
    }

    public static List<String> readFileAsList(GenericFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new java.io.FileReader(file.getPath()));
        List<String> content = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            content.add(line);
        }

        return(content);

    }

    public static Set<String> readFileAsSet(GenericFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new java.io.FileReader(file.getPath()));
        Set<String> content = new HashSet<>();

        String line;
        while ((line = reader.readLine()) != null) {
            content.add(line);
        }

        return(content);
    }

    public static GenotypeMatrix readGenotypeData(CommandLine cmd) throws IOException {
        RandomAccessGenotypeDataReaderFormats format = RandomAccessGenotypeDataReaderFormats.valueOfSmart(cmd.getOptionValue('t').trim());

        RandomAccessGenotypeData gt = null;

        switch (format) {
            case GEN:
                throw new UnsupportedOperationException("Not yet implemented");
            case GEN_FOLDER:
                throw new UnsupportedOperationException("Not yet implemented");
            case PED_MAP:
                throw new UnsupportedOperationException("Not yet implemented");
            case PLINK_BED:
                //throw new UnsupportedOperationException("Not yet implemented");
                gt = new BedBimFamGenotypeData(cmd.getOptionValue('g').trim());
                break;
            case SHAPEIT2:
                throw new UnsupportedOperationException("Not yet implemented");
            case TRITYPER:
                throw new UnsupportedOperationException("Not yet implemented");
            case VCF:
                throw new UnsupportedOperationException("Not yet implemented");
            case VCF_FOLDER:
                throw new UnsupportedOperationException("Not yet implemented");
        }


        GenericFile variantIdFile = new GenericFile(cmd.getOptionValue('r').trim());
        Set<String> variantIds = FileReader.readFileAsSet(variantIdFile);

        VariantFilter filter = new VariantIdIncludeFilter(variantIds);

        GenotypeData gtFiltered = new VariantFilterableGenotypeDataDecorator(gt, filter);

        Set<String> genotypeVariantIds = gt.getVariantIdMap().keySet();
        genotypeVariantIds.retainAll(variantIds);

        if (!gt.getVariantIdMap().keySet().containsAll(variantIds)) {
            LOGGER.warn("Not all SNPs found in genotype data");
            Set<String> missing = new HashSet<>();
            missing.addAll(variantIds);
            missing.removeAll(gt.getVariantIdMap().keySet());
            LOGGER.warn(missing);
        }

        GenotypeMatrix genotypeMatrix = new GenotypeMatrix(gtFiltered, genotypeVariantIds.size());

        return(genotypeMatrix);
    }

}
