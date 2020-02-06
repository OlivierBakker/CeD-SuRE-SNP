package nl.umcg.suresnp.pipeline.tools.parameters;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.IOException;

import static java.lang.System.exit;

public class MakeBarcodeCountsParameters {
    private final Logger LOGGER = Logger.getLogger(MakeBarcodeCountsParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String[] inputBarcodes;
    private String outputPrefix;
    private boolean writeBarcodeFile;

    // General arguments
    private String toolType;

    // Tool specific arguments
    private int barcodeLength;
    private int adapterMaxMismatch;
    private boolean trimFivePrimeToBarcodeLength;

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
                .longOpt("barcode-info")
                .hasArg(true)
                .desc("Cutadapt --info file. Argument may be supplied multiple times.")
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

        option = Option.builder("b")
                .longOpt("no-barcode-file")
                .hasArg(false)
                .desc("Don't write a file containing all (non-unique) valid barcodes. " +
                        "Can be used for BC overlapping or determining the complexity curve")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);

    }

    public MakeBarcodeCountsParameters(String[] args) throws ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            printHelp();
            exit(0);
        }

        // Input files
        if (cmd.hasOption('i')) {
            inputBarcodes = cmd.getOptionValues('i');
        }

        toolType = "MakeBarcodeCounts";

        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            outputPrefix = "ipcrtools";
        }

        // When writing to a file check if the correct options are specified
        if (!cmd.hasOption("o")) {
            LOGGER.error("-o not specified");
            printHelp();
            exit(1);
        }

        writeBarcodeFile = !cmd.hasOption("b");

        // Hardcoded arguments for testing
        barcodeLength = 20;
        adapterMaxMismatch = 3;
        trimFivePrimeToBarcodeLength = true;
    }


    public boolean isWriteBarcodeFile() {
        return writeBarcodeFile;
    }

    public String[] getInputBarcodes() {
        return inputBarcodes;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public int getBarcodeLength() {
        return barcodeLength;
    }

    public int getAdapterMaxMismatch() {
        return adapterMaxMismatch;
    }

    public boolean isTrimFivePrimeToBarcodeLength() {
        return trimFivePrimeToBarcodeLength;
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
