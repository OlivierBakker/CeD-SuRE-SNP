package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.ipcrwriter.*;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.SampleSumScoreProvider;
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
    private String[] samplesToWrite;

    private boolean writeDiscardedReads;
    private boolean noHeader;

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
                .argName("IPCR|IPCR_INDEXED|MACS|BED|BEDPE")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("n")
                .longOpt("no-header")
                .hasArg(false)
                .desc("Don't write the header")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("d")
                .longOpt("no-discarded-output")
                .hasArg(false)
                .desc("Don't discared output")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("s")
                .longOpt("samples-to-write")
                .hasArg(true)
                .desc("cDNA sample(s) to use to write MACS output. When providing multiple -s options, samples are summed")
                .argName("sample")
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
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            printHelp();
            exit(0);
        }

        inputFile = cmd.getOptionValue("i").trim();

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

        if (cmd.hasOption('s')) {
            samplesToWrite = cmd.getOptionValues('s');
        } else {
            samplesToWrite = null;
        }


        boolean zipped = cmd.hasOption("z");
        noHeader = cmd.hasOption("n");
        writeDiscardedReads = cmd.hasOption("d");

        switch (outputType) {
            case "BEDPE":
                if (barcodeCountFiles != null) {
                    outputWriter = new BedpeIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles, null);
                } else {
                    outputWriter = new BedpeIpcrRecordWriter(new File(outputPrefix), zipped, null);
                }
                break;
            case "BED":
                if (barcodeCountFiles != null) {
                    outputWriter = new BedIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles, null);
                } else {
                    outputWriter = new BedIpcrRecordWriter(new File(outputPrefix), zipped, null);
                }
                break;
            case "IPCR":
                if (barcodeCountFiles != null) {
                    outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles);
                } else {
                    outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "IPCR_INDEXED":
                if (barcodeCountFiles != null) {
                    outputWriter = new BlockCompressedIpcrRecordWriter(outputPrefix, barcodeCountFiles);
                } else {
                    outputWriter = new BlockCompressedIpcrRecordWriter(outputPrefix);
                }
                break;
            case "MACS":
                AdaptableScoreProvider provider = null;
                if (samplesToWrite == null) {
                    LOGGER.info("No sample provided but output type is MACS. Writing one bedfile for each cDNA sample");
                    printHelp();
                    exit(1);
                } else {
                    provider = new SampleSumScoreProvider(samplesToWrite);
                }

                if (barcodeCountFiles != null) {
                    outputWriter = new MacsIpcrRecordWriter(new File(outputPrefix), zipped, barcodeCountFiles, provider, true);
                } else {
                    outputWriter = new MacsIpcrRecordWriter(new File(outputPrefix), zipped, provider, true);
                }
                break;

            default:
                LOGGER.error("Invalid output type -t");
                printHelp();
                exit(1);
        }

    }

    public boolean isNoHeader() {
        return noHeader;
    }

    public boolean isWriteDiscardedReads() {
        return writeDiscardedReads;
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
