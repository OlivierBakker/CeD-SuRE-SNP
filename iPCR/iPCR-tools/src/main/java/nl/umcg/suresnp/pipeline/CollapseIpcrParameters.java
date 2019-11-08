package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.ipcrwriter.BedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.BedpeIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.GenericIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class CollapseIpcrParameters {
    private final Logger LOGGER = Logger.getLogger(MakeIpcrFileParameters.class);
    private final CommandLine cmd;

    private String inputFile;
    private String[] barcodeCountFiles;


    private String outputPrefix;
    private String outputSuffix;
    private String outputType;
    private IpcrOutputWriter outputWriter;
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
                .longOpt("input-ipcr")
                .hasArg(true)
                .desc("Input ipcr file generated using MakeIpcrFile")
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

        option = Option.builder("t")
                .longOpt("output-type")
                .hasArg(true)
                .desc("Output type")
                .argName("IPCR|BED|BEDPE")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);
    }


    public CollapseIpcrParameters(String[] args) throws IOException, ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(CollapseIpcrParameters.getOptions(), args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            MakeIpcrFileParameters.printHelp();
            exit(0);
        }

        inputFile=cmd.getOptionValue("i").trim();


        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            outputPrefix = "ipcrtools";
        }

        if (cmd.hasOption('t')) {
            outputType = cmd.getOptionValue('t').trim();
        } else {
            outputType = "IPCR";
        }


        boolean zipped = false;
        if (cmd.hasOption("z")) {
            zipped = true;
        }

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
            case "IPCR":
                if (barcodeCountFiles != null) {
                    outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles);
                } else {
                    outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix), zipped);
                }
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

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String[] getBarcodeCountFiles() {
        return barcodeCountFiles;
    }

    public void setBarcodeCountFiles(String[] barcodeCountFiles) {
        this.barcodeCountFiles = barcodeCountFiles;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public void setOutputPrefix(String outputPrefix) {
        this.outputPrefix = outputPrefix;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public void setOutputSuffix(String outputSuffix) {
        this.outputSuffix = outputSuffix;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public IpcrOutputWriter getOutputWriter() {
        return outputWriter;
    }

    public void setOutputWriter(IpcrOutputWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    public String getToolType() {
        return toolType;
    }

    public void setToolType(String toolType) {
        this.toolType = toolType;
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
