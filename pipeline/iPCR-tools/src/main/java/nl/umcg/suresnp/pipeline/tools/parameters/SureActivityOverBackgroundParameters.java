package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ReferenceBedFileType;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.util.HashMap;

import static java.lang.System.exit;

public class SureActivityOverBackgroundParameters {

    private final Logger LOGGER = Logger.getLogger(SureActivityOverBackgroundParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String[] inputIpcr;

    private GenericFile queryRegions;
    private GenericFile targetRegions;

    private String outputPrefix;

    // General arguments
    private String toolType;
    private int numberOfPermutations;
    private boolean trimChrFromContig;

    private String[] samplesToWrite;

    private static final Options OPTIONS;

    static {
        OPTIONS = new Options();
        Option option;

        option = Option.builder("T")
                .longOpt("tool")
                .hasArg(true)
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("i")
                .longOpt("input")
                .hasArg(true)
                .desc("The input file(s)")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("q")
                .longOpt("query-file")
                .hasArg(true)
                .desc("The input file")
                .argName("<path/to/file>")
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
                .desc("Output prefix")
                .argName("path/to/output")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("s")
                .longOpt("samples-to-write")
                .hasArg(true)
                .desc("cDNA sample(s) to use to write MACS|BED output. When providing more then one -s, samples are summed up.")
                .argName("sample")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("tchr")
                .longOpt("trim-chr-from-contig")
                .hasArg(false)
                .desc("Should the preceding 'chr' be removed from the chromosome id's when reading the bed files.")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);

    }

    public SureActivityOverBackgroundParameters(String args[]) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            printHelp();
            exit(0);
        }

        // Input iPCR files
        if (cmd.hasOption('i')) {
            inputIpcr = cmd.getOptionValues('i');
        } else {
            LOGGER.error("No iPCR file(s) provided");
            exit(-1);
        }

        if (cmd.hasOption('s')) {
            samplesToWrite = cmd.getOptionValues('s');
        } else {
            LOGGER.error("No cDNA sample(s) provided");
            exit(-1);
        }

        // Query file
        if (cmd.hasOption('q')) {
            queryRegions = new GenericFile(cmd.getOptionValue("q"));
        } else {
            LOGGER.error("Missing query file");
            exit(-1);
        }

        // Limit analysis to these regions
        if (cmd.hasOption("f")) {
            targetRegions = new GenericFile(cmd.getOptionValue("f"));
        } else {
            LOGGER.error("No target regions (-f) provided");
            exit(-1);
        }

        // Output prefix
        if (cmd.hasOption("o")) {
            outputPrefix = cmd.getOptionValue("o");
        } else {
            outputPrefix = "ipcrtools_genomic_region_enrichment";
        }

        // Number of permutations
        if (cmd.hasOption("p")) {
            numberOfPermutations = Integer.parseInt(cmd.getOptionValue("p"));
        } else {
            numberOfPermutations = 10000;
        }

        // Trim the preceding chr from bed file contig names
        trimChrFromContig = cmd.hasOption("tchr");
    }

    public String[] getInputIpcr() {
        return inputIpcr;
    }

    public GenericFile getQueryRegions() {
        return queryRegions;
    }

    public GenericFile getTargetRegions() {
        return targetRegions;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public String getToolType() {
        return toolType;
    }

    public int getNumberOfPermutations() {
        return numberOfPermutations;
    }

    public boolean isTrimChrFromContig() {
        return trimChrFromContig;
    }

    public String[] getSamplesToWrite() {
        return samplesToWrite;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }
}
