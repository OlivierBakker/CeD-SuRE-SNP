package nl.umcg.suresnp.pipeline.tools.parameters;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.IOException;

import static java.lang.System.exit;

public class MakeSummariesParameters {
    private final Logger LOGGER = Logger.getLogger(MakeBarcodeComplexityCurveParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String[] inputIpcr;
    private String inputBarcodes;

    // Yes, should be an enum, but I couldn't be bothered
    private String inputType;

    private String outputPrefix;
    private String outputSuffix;

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

        option = Option.builder("t")
                .longOpt("input-type")
                .hasArg(true)
                .desc("The input type")
                .argName("IPCR|INFO")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("b")
                .longOpt("barcode-info")
                .hasArg(true)
                .desc("The file containing read names and barcodes")
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


    public MakeSummariesParameters(String[] args) throws ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            MakeBarcodeComplexityCurveParameters.printHelp();
            exit(0);
        }

        // Input files
        if (cmd.hasOption('i')) {
            inputIpcr = cmd.getOptionValues('i');
        }

        if (cmd.hasOption('t')) {
            inputType = cmd.getOptionValue('t');

            switch (inputType) {
                case "IPCR":
                    break;
                case "INFO":
                    break;
                default:
                    LOGGER.error("Invalid input type, must be either IPCR or INFO");
                    printHelp();
                    exit(-1);
            }
        } else {
            inputType = "IPCR";
        }

        if (cmd.hasOption('b')) {
            inputBarcodes = cmd.getOptionValue('b').trim();
        }
        toolType = "MakeSummaries";

        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            outputPrefix = "ipcrtools";
        }

        // When writing to a file check if the correct options are specified
        if (!cmd.hasOption("o")) {
            LOGGER.error("-o not specified");
            MakeSummariesParameters.printHelp();
            exit(1);
        }

        boolean zipped = false;
        outputSuffix = "";
        if (cmd.hasOption("z")) {
            zipped = true;
            outputSuffix = ".gz";
        }


    }

    public CommandLine getCmd() {
        return cmd;
    }

    public String getInputBarcodes() {
        return inputBarcodes;
    }

    public String[] getInputIpcr(){ return inputIpcr;}

    public String getInputType(){ return inputType;}

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public String getOutputSuffix() {
        return outputSuffix;
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


}
