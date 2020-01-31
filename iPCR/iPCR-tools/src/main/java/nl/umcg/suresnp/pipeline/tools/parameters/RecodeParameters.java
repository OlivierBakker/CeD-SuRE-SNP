package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.ipcrwriter.*;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class RecodeParameters {

    private final Logger LOGGER = Logger.getLogger(RecodeParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String[] inputIpcr;
    // Yes, should be an enum, but I couldn't be bothered
    private String inputType;

    // Output
    private String outputPrefix;
    private String outputType;
    private IpcrOutputWriter outputWriter;
    private String[] barcodeCountFiles;
    private boolean isStdoutput;

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

        option = Option.builder("f")
                .longOpt("input-type")
                .hasArg(true)
                .desc("The input type")
                .argName("IPCR|IPCR_BIN")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("t")
                .longOpt("output-type")
                .hasArg(true)
                .desc("The output type")
                .argName("IPCR|IPCR_BIN|MACS|BED|BEDPE|BEDGRAPH")
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

    public RecodeParameters(String[] args) throws ParseException, IOException {
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

        if (cmd.hasOption('f')) {
            inputType = cmd.getOptionValue('f');
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

        toolType = "Recode";

        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            LOGGER.error("-o not specified");
            RecodeParameters.printHelp();
            exit(1);
        }

        if (cmd.hasOption('t')) {
            outputType = cmd.getOptionValue('t').trim();
        } else {
            outputType = "IPCR";
        }

        boolean zipped = cmd.hasOption("z");

        switch (outputType) {
            case "BEDPE":
                if (barcodeCountFiles != null) {
                    outputWriter = new BedpeIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles);
                } else {
                    outputWriter = new BedpeIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "BED":
                if (barcodeCountFiles != null) {
                    outputWriter = new BedIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles);
                } else {
                    outputWriter = new BedIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "BEDGRAPH":
                if (barcodeCountFiles != null) {
                    outputWriter = new BedGraphIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles);
                } else {
                    outputWriter = new BedGraphIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "IPCR":
                if (barcodeCountFiles != null) {
                    outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles);
                } else {
                    outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "MACS":
                if (barcodeCountFiles != null) {
                    outputWriter = new MacsIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles);
                } else {
                    outputWriter = new MacsIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "IPCR_BIN":
                outputWriter = new BinaryIpcrRecordWriter(new File(outputPrefix), zipped);
                break;
            default:
                LOGGER.error("Invalid output type -t");
                printHelp();
                exit(1);
        }

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

    public String getOutputType() {
        return outputType;
    }

    public IpcrOutputWriter getOutputWriter() {
        return outputWriter;
    }

    public String[] getBarcodeCountFiles() {
        return barcodeCountFiles;
    }

    public boolean isStdoutput() {
        return isStdoutput;
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
