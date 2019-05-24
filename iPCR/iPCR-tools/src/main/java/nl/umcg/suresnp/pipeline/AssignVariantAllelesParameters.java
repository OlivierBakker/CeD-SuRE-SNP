package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.icpr.GenericIpcrRecordStdoutWriter;
import nl.umcg.suresnp.pipeline.io.icpr.GenericIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrOutputWriter;
import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class AssignVariantAllelesParameters {

    private final Logger LOGGER = Logger.getLogger(GenerateBarcodeComplexityCurveParameters.class);
    private final CommandLine cmd;


    // IO arguments
    private String inputBam;
    private String inputBarcodes;
    private String inputVcf;

    private String outputPrefix;
    private String outputSuffix;

    private boolean isStdoutput;
    private boolean isReduced;
    private IpcrOutputWriter outputWriter;

    // General arguments
    private String toolType;


    // Tool specific arguments
    private int barcodeLength;
    private int adapterMaxMismatch;


    private static final Options OPTIONS;

    static {
        OPTIONS = new Options();
        Option option;

        option = Option.builder("i")
                .longOpt("input-bam")
                .hasArg(true)
                .desc("Input bamfile")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("g")
                .longOpt("input-genotype")
                .hasArg(true)
                .desc("Currently only supports VCF")
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

        option = Option.builder("s")
                .longOpt("stdout")
                .desc("Pipe output to stdout instead of to a file. Will omit logging of warnings, info and debug.")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("r")
                .longOpt("reduced")
                .desc("Print reduced output (without the sequences, cigars and variant info. " +
                        "Variant info is written to a separate file. Can be used to save space when using a large library)")
                .build();
        OPTIONS.addOption(option);

    }


    public AssignVariantAllelesParameters(String[] args) throws ParseException, IOException {

        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(GenerateBarcodeComplexityCurveParameters.getOptions(), args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            GenerateBarcodeComplexityCurveParameters.printHelp();
            exit(0);
        }

        // Input files
        inputBam = cmd.getOptionValue("i").trim();
        inputVcf = cmd.getOptionValue("g").trim();
        inputBarcodes = cmd.getOptionValue('b').trim();
        toolType = "AssignVariantAlleles";

        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            outputPrefix = "ipcrtools";
        }

        if (cmd.hasOption("s")) {
            // When writing to stdout do not use log4j unless there is an error
            outputWriter = new GenericIpcrRecordStdoutWriter();

            Logger.getRootLogger().setLevel(Level.ERROR);

        } else {
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

            outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix + ".full.ipcr" + outputSuffix), zipped);
        }


        // Hardcoded arguments for testing
        barcodeLength = 20;
        adapterMaxMismatch = 3;

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

    public String getInputVcf() {
        return inputVcf;
    }

    public boolean isStdoutput() {
        return isStdoutput;
    }

    public boolean isReduced() {
        return isReduced;
    }

    public static Options getOPTIONS() {
        return OPTIONS;
    }

    public IpcrOutputWriter getOutputWriter() {
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
