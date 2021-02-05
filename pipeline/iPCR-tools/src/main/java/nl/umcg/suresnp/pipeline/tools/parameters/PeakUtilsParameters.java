package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.filters.NarrowPeakFilter;
import nl.umcg.suresnp.pipeline.records.bedrecord.filters.ScoreGtThanFilter;
import nl.umcg.suresnp.pipeline.records.bedrecord.filters.SignalValueGtThanFilter;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class PeakUtilsParameters {

    private final Logger LOGGER = Logger.getLogger(PeakUtilsParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private GenericFile[] inputFiles;
    private String outputPrefix;
    private List<NarrowPeakFilter> filters;
    private double ipcrCountFilter;
    private String pattern;

    // General arguments
    private String toolType;
    private boolean discardUnique;
    private String[] inputIpcr;
    private String[] samplesToWrite;
    private int threads;

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

        option = Option.builder("t")
                .longOpt("trim-pattern")
                .hasArg(true)
                .desc("Regex pattern to use to trim peaknames")
                .argName("pattern")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("o")
                .longOpt("output")
                .hasArg(true)
                .desc("Output prefix")
                .argName("path/to/output")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("threads")
                .longOpt("threads")
                .hasArg(true)
                .desc("Number of threads to run some functionality in parallel")
                .argName("[0-9]")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("fs")
                .longOpt("score-filter")
                .hasArg(true)
                .desc("Relative enrichment to filter on")
                .argName("[0-9]")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("ff")
                .longOpt("fragment-filter")
                .hasArg(true)
                .desc("Minimal amount of plasmids to consider to use a consensus peak (GetPeakCounts only)")
                .argName("[0-9]")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("u")
                .longOpt("discard-unique")
                .hasArg(false)
                .desc("Discard unique peaks")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("ipcr")
                .longOpt("input-ipcr")
                .hasArg(true)
                .desc("Block compressed iPCR file to derive the cDNA scores from. Argument may be specified multiple" +
                        "times to argegate multiple iPCR files.")
                .argName("<file>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("s")
                .longOpt("samples-fragment-filter")
                .hasArg(true)
                .desc("cDNA sample(s) to use for --fragment-filter. If missing all iPCR fragments are used. If provided" +
                        "multiple times the scores are summed. This argument is used to filter all fragments that do not" +
                        "have cDNA activity but do overlap a peak.")
                .argName("<sample id>")
                .build();
        OPTIONS.addOption(option);


        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);
    }


    public PeakUtilsParameters(String[] args) throws ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            printHelp();
            exit(0);
        }

        // Input files
        if (cmd.hasOption('i')) {
            inputFiles = new GenericFile[cmd.getOptionValues('i').length];
            for (int i=0; i < inputFiles.length; i ++) {
                inputFiles[i] = new GenericFile(cmd.getOptionValues('i')[i]);
            }
        }

        // Filters
        filters = new ArrayList<>();

        if (cmd.hasOption("fs")) {
            filters.add(new SignalValueGtThanFilter(Double.parseDouble(cmd.getOptionValue("fs"))));
        }

        LOGGER.info("Applying the following filters BEFORE intersecting:");
        for (NarrowPeakFilter filter: filters) {
            LOGGER.info(filter.getFilterType());
        }

        toolType = "PeakUtils";
        discardUnique = !cmd.hasOption("u");

        if (cmd.hasOption("t")) {
            pattern = cmd.getOptionValue("t");
        }

        if (cmd.hasOption("ipcr")) {
            inputIpcr = cmd.getOptionValues("ipcr");
        }

        if (cmd.hasOption("ff")) {
            ipcrCountFilter = Double.parseDouble(cmd.getOptionValue("ff"));
        }

        if (cmd.hasOption('s')) {
            samplesToWrite = cmd.getOptionValues('s');
        } else {
            samplesToWrite = null;
        }

        if (cmd.hasOption("threads")) {
            threads = Integer.parseInt(cmd.getOptionValue("threads"));
        } else {
            threads = 1;
        }
        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            outputPrefix = "ipcrtools";
        }
    }

    public CommandLine getCmd() {
        return cmd;
    }

    public GenericFile[] getInputFiles(){ return inputFiles;}

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public List<NarrowPeakFilter> getFilters() {
        return filters;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isDiscardUnique() {
        return discardUnique;
    }

    public String[] getInputIpcr() {
        return inputIpcr;
    }

    public String[] getSamplesToWrite() {
        return samplesToWrite;
    }

    public static Options getOPTIONS() {
        return OPTIONS;
    }

    public String getToolType() {
        return toolType;
    }

    public static Options getOptions() {
        return OPTIONS;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }

    public double getIpcrCountFilter() {
        return ipcrCountFilter;
    }

    public void setIpcrCountFilter(double ipcrCountFilter) {
        this.ipcrCountFilter = ipcrCountFilter;
    }

    public boolean hasIpcrCountFilter() {
        return cmd.hasOption("ff");
    }

    public boolean hasInputIpcr() {
        return cmd.hasOption("ipcr");
    }

    public boolean hasSamplesToWrite() {
        return cmd.hasOption("s");
    }

    public int getThreads() {
        return threads;
    }
}
