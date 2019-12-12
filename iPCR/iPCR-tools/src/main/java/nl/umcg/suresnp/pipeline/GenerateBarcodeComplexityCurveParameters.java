package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.ipcrwriter.AlleleSpecificIpcrOutputWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.AlleleSpecificIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.GenericAlleleSpecificIpcrRecordStdoutWriter;
import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;
import static java.lang.System.in;


public class GenerateBarcodeComplexityCurveParameters {


    private final Logger LOGGER = Logger.getLogger(GenerateBarcodeComplexityCurveParameters.class);
    private final CommandLine cmd;


    // IO arguments
    private String inputBam;
    private String inputBarcodes;
    private boolean simpleReader;

    private String outputPrefix;
    private String outputSuffix;

    private boolean isStdoutput;
    private AlleleSpecificIpcrOutputWriter outputWriter;

    // General arguments
    private String toolType;

    // Tool specific arguments
    private int barcodeLength;
    private int adapterMaxMismatch;

    private int nDownSampleIntervals;
    private int nIterations;
    private int intervalMax;

    private static final Options OPTIONS;

    static {
        OPTIONS = new Options();
        Option option;

        option = Option.builder("T")
                .longOpt("tool")
                .hasArg(true)
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

        option = Option.builder("m")
                .longOpt("max-records")
                .hasArg(true)
                .desc("Ammount of reacords to read")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("s")
                .longOpt("simple-reader")
                .desc("Barcode input file is not --info-file from cutadapt, but only contains barcode seqeunces.")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);

    }


    public GenerateBarcodeComplexityCurveParameters(String[] args) throws ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            GenerateBarcodeComplexityCurveParameters.printHelp();
            exit(0);
        }

        // Input files
        if (cmd.hasOption('b')) {
            inputBarcodes = cmd.getOptionValue('b').trim();
        }
        toolType = "GenerateBarcodeComplexityCurve";

        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            outputPrefix = "ipcrtools";
        }

        simpleReader= cmd.hasOption("s");

        // When writing to a file check if the correct options are specified
        if (!cmd.hasOption("o")) {
            LOGGER.error("-o not specified");
            GenerateBarcodeComplexityCurveParameters.printHelp();
            exit(1);
        }

        boolean zipped = false;
        outputSuffix = "";
        if (cmd.hasOption("z")) {
            zipped = true;
            outputSuffix = ".gz";
        }

        //outputWriter = new AlleleSpecificIpcrRecordWriter(new File(outputPrefix + ".full.ipcr" + outputSuffix), zipped);
        // Hardcoded arguments for testing
        barcodeLength = 20;
        adapterMaxMismatch = 3;

        // How may intervals to divide the total readcount by
        nDownSampleIntervals = 20;

        // How many times to repeat and average the values
        nIterations = 1;

        // Input files
        if (cmd.hasOption('m')) {
            intervalMax = Integer.parseInt(cmd.getOptionValue('m').trim());
        } else {
            intervalMax = 1000000;
            LOGGER.warn("Intitialzing intervalMax at " + intervalMax);
        }
    }


    public int getIntervalMax() {
        return intervalMax;
    }

    public int getnDownSampleIntervals() {
        return nDownSampleIntervals;
    }

    public int getnIterations() {
        return nIterations;
    }

    public int getAdapterMaxMismatch() {
        return adapterMaxMismatch;
    }

    public int getBarcodeLength() {
        return barcodeLength;
    }

    public String getInputBam() {
        return inputBam;
    }

    public CommandLine getCmd() {
        return cmd;
    }

    public String getInputBarcodes() {
        return inputBarcodes;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public boolean isStdoutput() {
        return isStdoutput;
    }


    public boolean isSimpleReader() {
        return simpleReader;
    }

    public static Options getOPTIONS() {
        return OPTIONS;
    }

    public AlleleSpecificIpcrOutputWriter getOutputWriter() {
        return outputWriter;
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
