package nl.umcg.suresnp.pipeline.tools.parameters;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import static java.lang.System.exit;

public class SubsetBamParameters {

    private final Logger LOGGER = Logger.getLogger(SubsetBamParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String[] inputIpcr;
    private String[] inputBam;

    // Yes, should be an enum, but I couldn't be bothered
    private String inputType;

    // Output
    private String outputPrefix;
    private boolean sortAndIndex;

    // General arguments
    private String toolType;

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

        option = Option.builder("k")
                .longOpt("input-type")
                .hasArg(true)
                .desc("The input type")
                .argName("IPCR|IPCR_BIN")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("s")
                .longOpt("sort-and-index")
                .hasArg(false)
                .desc("Should the BAM be coordinate sorted and indexed? Slower and more memory intensive")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("b")
                .longOpt("input-bam")
                .hasArg(true)
                .desc("BAM files")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("o")
                .longOpt("output")
                .hasArg(true)
                .desc("Output prefix")
                .argName("path/to/output")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);

    }

    public SubsetBamParameters(String[] args) throws ParseException {
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
        }

        // Input BAM files
        if (cmd.hasOption('b')) {
            inputBam = cmd.getOptionValues('b');
        }

        // Input type
        if (cmd.hasOption('k')) {
            inputType = cmd.getOptionValue('k');
            switch (inputType) {
                case "IPCR":
                    break;
                case "IPCR_BIN":
                    break;
                default:
                    LOGGER.error("Invalid input type, must be either IPCR, IPCR_BIN");
                    printHelp();
                    exit(-1);
            }
        } else {
            inputType = "IPCR";
        }

        // Should BAM be sorted on coordinates and indexed
        sortAndIndex = cmd.hasOption('s');

        // Set output prefix
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            LOGGER.error("-o not specified");
            printHelp();
            exit(1);
        }

        toolType = "SubsetBam";

    }

    public CommandLine getCmd() {
        return cmd;
    }

    public String[] getInputIpcr() {
        return inputIpcr;
    }

    public String getInputType() {
        return inputType;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public boolean isSortAndIndex() {
        return sortAndIndex;
    }

    public String[] getInputBam() {
        return inputBam;
    }

    public String getToolType() {
        return toolType;
    }

    public static Options getOPTIONS() {
        return OPTIONS;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }

}
