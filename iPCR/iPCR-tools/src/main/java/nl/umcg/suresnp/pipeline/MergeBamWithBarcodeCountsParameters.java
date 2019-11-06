package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.icpr.BedIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.icpr.BedpeIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.icpr.GenericIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrOutputWriter;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.exit;


public class MergeBamWithBarcodeCountsParameters {

    private final Logger LOGGER = Logger.getLogger(MergeBamWithBarcodeCountsParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String inputBam;
    private String inputBarcodes;
    private String inputBarcodeCounts;
    private String secondaryInputBam;

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

    private static final ArrayList<String> chromosomes;

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

        chromosomes = new ArrayList<String>() {{
            add("1");
            add("2");
            add("3");
            add("4");
            add("5");
            add("6");
            add("7");
            add("8");
            add("9");
            add("10");
            add("11");
            add("12");
            add("13");
            add("14");
            add("15");
            add("16");
            add("17");
            add("18");
            add("19");
            add("20");
            add("21");
            add("22");
            add("x");
            add("y");
            add("X");
            add("Y");
            add("chr1");
            add("chr2");
            add("chr3");
            add("chr4");
            add("chr5");
            add("chr6");
            add("chr7");
            add("chr8");
            add("chr9");
            add("chr10");
            add("chr11");
            add("chr12");
            add("chr13");
            add("chr14");
            add("chr15");
            add("chr16");
            add("chr17");
            add("chr18");
            add("chr19");
            add("chr20");
            add("chr21");
            add("chr22");
            add("chrx");
            add("chry");
            add("chrX");
            add("chrY");
        }};
    }

    public MergeBamWithBarcodeCountsParameters(String[] args) throws ParseException, IOException {

        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(MergeBamWithBarcodeCountsParameters.getOptions(), args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            MergeBamWithBarcodeCountsParameters.printHelp();
            exit(0);
        }

        // Input files
        inputBam = cmd.getOptionValue("i").trim();
        inputBarcodes = cmd.getOptionValue('b').trim();
        toolType = "MergeBamWithBarcodeCounts";

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

        if (cmd.hasOption("s")) {
            // When writing to stdout do not use log4j unless there is an error
            //outputWriter = new GenericIpcrRecordStdoutWriter();
            //Logger.getRootLogger().setLevel(Level.ERROR);
            LOGGER.error("Not yet implemented");
        } else {
            // When writing to a file check if the correct options are specified
            if (!cmd.hasOption("o")) {
                LOGGER.error("-o not specified");
                MergeBamWithBarcodeCountsParameters.printHelp();
                exit(1);
            }

            boolean zipped = false;
            outputSuffix = "";
            if (cmd.hasOption("z")) {
                zipped = true;
                outputSuffix = ".gz";
            }


            switch (outputType) {
                case "BEDPE":
                    if (barcodeCountFiles != null) {
                        outputWriter = new BedpeIpcrRecordWriter(new File(outputPrefix + outputSuffix), zipped, barcodeCountFiles);
                    } else {
                        outputWriter = new BedpeIpcrRecordWriter(new File(outputPrefix + outputSuffix), zipped);
                    }
                    break;
                case "BED":
                    if (barcodeCountFiles != null) {
                        outputWriter = new BedIpcrRecordWriter(new File(outputPrefix + outputSuffix), zipped, barcodeCountFiles);
                    } else {
                        outputWriter = new BedIpcrRecordWriter(new File(outputPrefix + outputSuffix), zipped);
                    }
                    break;
                case "IPCR":
                    if (barcodeCountFiles != null) {
                        outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix + ".full.ipcr" + outputSuffix), zipped, barcodeCountFiles);
                    } else {
                        outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix + ".full.ipcr" + outputSuffix), zipped);
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

    public static ArrayList<String> getChromosomes() {
        return chromosomes;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }
}
