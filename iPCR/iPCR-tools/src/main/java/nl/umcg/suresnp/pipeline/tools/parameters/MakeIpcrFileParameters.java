package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.BedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.BedpeIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.GenericIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.exit;


public class MakeIpcrFileParameters {

    private final Logger LOGGER = Logger.getLogger(MakeIpcrFileParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String inputBam;
    private String inputBarcodes;
    private String inputBarcodeCounts;
    private String secondaryInputBam;
    private GenericFile regionFilterFile;

    private String outputPrefix;
    private String outputSuffix;
    private String outputType;

    private boolean isStdoutput;
    private boolean isReduced;
    private IpcrOutputWriter outputWriter;

    // General arguments
    private String toolType;

    // Tool specific arguments
    private int barcodeLength;
    private int adapterMaxMismatch;

    private String[] barcodeCountFiles;

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
                .longOpt("input-bam")
                .hasArg(true)
                .desc("Input bamfile, for the SuRE-SNP pipeline use the GATK --bamout file")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

/*        option = Option.builder("j")
                .longOpt("secondary-input-bam")
                .hasArg(true)
                .desc("Secondary input bamfile, Used to assign reads when not in primary bam file." +
                        "Primary bamfile has higher priority, so in case of a duplicate, the primary is used." +
                        "This is used for assigning reads from homzoygous reference samples which are not in the --bamout file")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);*/

        option = Option.builder("b")
                .longOpt("barcode-info")
                .hasArg(true)
                .desc("The file containing read names and barcodes")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("n")
                .longOpt("barcode-counts")
                .hasArg(true)
                .desc("RNAseq based barcode counts")
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

        option = Option.builder("f")
                .longOpt("region-filter")
                .hasArg(true)
                .desc("BED file to only output records overlapping those regions")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("t")
                .longOpt("output-type")
                .hasArg(true)
                .desc("Output type")
                .argName("BED|IPCR")
                .build();
        OPTIONS.addOption(option);

     /*   option = Option.builder("s")
                .longOpt("stdout")
                .desc("Pipe output to stdout instead of to a file. Will omit logging of warnings, info and debug.")
                .build();
        OPTIONS.addOption(option);*/

        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);
/*
        option = Option.builder("r")
                .longOpt("reduced")
                .desc("Print reduced output (without the sequences, cigars and variant info. " +
                        "Variant info is written to a separate file. Can be used to save space when using a large library)")
                .build();
        OPTIONS.addOption(option);*/

    }

    public MakeIpcrFileParameters(String[] args) throws ParseException, IOException {

        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(MakeIpcrFileParameters.getOptions(), args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            MakeIpcrFileParameters.printHelp();
            exit(0);
        }

        // Input files
        inputBam = cmd.getOptionValue("i").trim();
        inputBarcodes = cmd.getOptionValue('b').trim();
        toolType = "MakeIpcrFile";

        if (cmd.hasOption("j")) {
            secondaryInputBam = cmd.getOptionValue("j").trim();
        }

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

        if (cmd.hasOption("n")) {
            barcodeCountFiles = cmd.getOptionValues("n");
        }

        if (cmd.hasOption("f")) {
            regionFilterFile = new GenericFile(cmd.getOptionValue("f"));
        }

        if (cmd.hasOption("s")) {
            // When writing to stdout do not use log4j unless there is an error
            //outputWriter = new GenericIpcrRecordStdoutWriter();
            //Logger.getRootLogger().setLevel(Level.ERROR);
            LOGGER.error("Not yet implemented");
        } else {
            // When writing to a file check if the correct options are specified
            if (!cmd.hasOption("o")) {
                LOGGER.error("-o not specified");
                MakeIpcrFileParameters.printHelp();
                exit(1);
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

        // Hardcoded arguments for testing
        barcodeLength = 20;
        adapterMaxMismatch = 3;
    }

    public CommandLine getCmd() {
        return cmd;
    }

    public boolean hasBarcodeCountFiles() {
        return barcodeCountFiles != null;
    }

    public String[] getBarcodeCountFiles() {
        return barcodeCountFiles;
    }

    public String getInputBam() {
        return inputBam;
    }

    public String getInputBarcodes() {
        return inputBarcodes;
    }

    public String getInputBarcodeCounts() {
        return inputBarcodeCounts;
    }

    public String getSecondaryInputBam() {
        return secondaryInputBam;
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

    public boolean isReduced() {
        return isReduced;
    }

    public IpcrOutputWriter getOutputWriter() {
        return outputWriter;
    }

    public int getBarcodeLength() {
        return barcodeLength;
    }

    public int getAdapterMaxMismatch() {
        return adapterMaxMismatch;
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

    public GenericFile getRegionFilterFile() {
        return regionFilterFile;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }
}
