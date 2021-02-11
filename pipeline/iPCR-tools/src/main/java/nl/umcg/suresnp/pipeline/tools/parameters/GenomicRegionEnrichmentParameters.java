package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ReferenceBedFileType;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.filters.NarrowPeakFilter;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

public class GenomicRegionEnrichmentParameters {

    private final Logger LOGGER = Logger.getLogger(GenomicRegionEnrichmentParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private GenericFile query;
    private Map<String, GenericFile> referenceFiles;
    private ReferenceBedFileType referenceBedFileType;
    private GenericFile targetRegions;

    private String outputPrefix;

    // General arguments
    private String toolType;
    private int numberOfPermutations;
    private boolean trimChrFromContig;

    private static final Options OPTIONS;

    static {
        OPTIONS = new Options();
        Option option;

        option = Option.builder("T")
                .longOpt("tool")
                .hasArg(true)
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("q")
                .longOpt("query-file")
                .hasArg(true)
                .desc("The input file")
                .argName("<path/to/file>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("r")
                .longOpt("reference-file")
                .hasArg(true)
                .desc("The input file.")
                .argName("<path/to/file>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("rt")
                .longOpt("reference-type")
                .hasArg(true)
                .desc("Determines the type of files provided. THREE_COL; all the records are read as being from the same" +
                        "database. FOUR_COL; the records are split into databases depending on the value in an additional collum." +
                        "THREE_COL can contained mixed filetypes as long as the first three columns are BED. FOUR_COL needs the" +
                        "column number in all files provided to be the same. If the type is" +
                        "FOUR_COL the database name from -r is added to the string in the split column.")
                .argName("THREE_COL (default) | FOUR_COL:<column #>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("f")
                .longOpt("restrict-analysis-to-region")
                .hasArg(true)
                .desc("The input file.")
                .argName("<path/to/file>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("p")
                .longOpt("nperm")
                .hasArg(true)
                .desc("The number of permutations. 10.000 by default")
                .argName("###")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("o")
                .longOpt("output")
                .hasArg(true)
                .desc("The output prefix.")
                .argName("<prefix>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("tchr")
                .longOpt("trim-chr-from-contig")
                .hasArg(false)
                .desc("Should the preceding 'chr' be removed from the chromosome id's when reading the bed files.")
                .build();
        OPTIONS.addOption(option);
    }

    public GenomicRegionEnrichmentParameters(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            printHelp();
            exit(0);
        }

        // Query file
        if (cmd.hasOption('q')) {
            query = new GenericFile(cmd.getOptionValue("q"));
        } else {
            LOGGER.error("Missing query file");
            exit(-1);
        }

        // Reference file
        if (cmd.hasOption('r')) {
            referenceFiles = new HashMap<>();

            for (int i = 0; i < cmd.getOptionValues('r').length; i++) {
                String currentValue = cmd.getOptionValues('r')[i];
                String[] keyPair = currentValue.split("=");
                if (keyPair.length != 2) {
                    LOGGER.error("Invalid value provided to -r: " + currentValue);
                    LOGGER.error("Must be in from -r <name>=<path>");
                    exit(-1);
                }
                referenceFiles.put(keyPair[0], new GenericFile(keyPair[1]));
            }
        } else {
            LOGGER.error("Missing reference file(s)");
            exit(-1);
        }


        if (cmd.hasOption("rt")) {
            String arg = cmd.getOptionValue("rt");

            if (arg.startsWith("THREE_COL")) {
                referenceBedFileType = new ReferenceBedFileType(ReferenceBedFileType.BedFileType.THREE_COL);
            } else if (arg.startsWith("FOUR_COL")) {
                int colNumber = Integer.parseInt(arg.split(":")[1]);
                referenceBedFileType = new ReferenceBedFileType(colNumber, ReferenceBedFileType.BedFileType.FOUR_COL);
            } else {
                LOGGER.error("Not a valid reference type");
                exit(-1);
            }
        } else {
            referenceBedFileType = new ReferenceBedFileType(ReferenceBedFileType.BedFileType.THREE_COL);
        }

        if (cmd.hasOption("f")) {
            targetRegions = new GenericFile(cmd.getOptionValue("f"));
        } else {
            LOGGER.error("No target regions (-f) provided");
            exit(-1);
        }


        if (cmd.hasOption("o")) {
            outputPrefix = cmd.getOptionValue("o");
        } else {
            outputPrefix = "ipcrtools_genomic_region_enrichment";
        }

        if (cmd.hasOption("p")) {
            numberOfPermutations = Integer.parseInt(cmd.getOptionValue("p"));
        } else {
            numberOfPermutations = 10000;
        }

        trimChrFromContig = cmd.hasOption("tchr");
    }


    public int getNumberOfPermutations() {
        return numberOfPermutations;
    }

    public GenericFile getQuery() {
        return query;
    }

    public Map<String, GenericFile> getReferenceFiles() {
        return referenceFiles;
    }

    public GenericFile getTargetRegions() {
        return targetRegions;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public ReferenceBedFileType getReferenceBedFileType() {
        return referenceBedFileType;
    }

    public boolean isTrimChrFromContig() {
        return trimChrFromContig;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }

}
