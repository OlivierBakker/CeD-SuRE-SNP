package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.ipcrwriter.*;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
                .argName("IPCR|MACS|BED|BEDPE")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("n")
                .longOpt("no-header")
                .hasArg(false)
                .desc("Don't write the header")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("h")
                .longOpt("help")
                .desc("Print usage")
                .build();
        OPTIONS.addOption(option);
    }

    private static final Map<String, Integer> chromSizes;

    static {
        chromSizes=new HashMap<>();
        chromSizes.put("chr1", 249250621);
        chromSizes.put("chr2", 243199373);
        chromSizes.put("chr3", 198022430);
        chromSizes.put("chr4", 191154276);
        chromSizes.put("chr5", 180915260);
        chromSizes.put("chr6", 171115067);
        chromSizes.put("chr7", 159138663);
        chromSizes.put("chrX", 155270560);
        chromSizes.put("chr8", 146364022);
        chromSizes.put("chr9", 141213431);
        chromSizes.put("chr10", 135534747);
        chromSizes.put("chr11", 135006516);
        chromSizes.put("chr12", 133851895);
        chromSizes.put("chr13", 115169878);
        chromSizes.put("chr14", 107349540);
        chromSizes.put("chr15", 102531392);
        chromSizes.put("chr16", 90354753);
        chromSizes.put("chr17", 81195210);
        chromSizes.put("chr18", 78077248);
        chromSizes.put("chr20", 63025520);
        chromSizes.put("chrY", 59373566);
        chromSizes.put("chr19", 59128983);
        chromSizes.put("chr22", 51304566);
        chromSizes.put("chr21", 48129895);
        chromSizes.put("1", 249250621);
        chromSizes.put("2", 243199373);
        chromSizes.put("3", 198022430);
        chromSizes.put("4", 191154276);
        chromSizes.put("5", 180915260);
        chromSizes.put("6", 171115067);
        chromSizes.put("7", 159138663);
        chromSizes.put("X", 155270560);
        chromSizes.put("8", 146364022);
        chromSizes.put("9", 141213431);
        chromSizes.put("10", 135534747);
        chromSizes.put("11", 135006516);
        chromSizes.put("12", 133851895);
        chromSizes.put("13", 115169878);
        chromSizes.put("14", 107349540);
        chromSizes.put("15", 102531392);
        chromSizes.put("16", 90354753);
        chromSizes.put("17", 81195210);
        chromSizes.put("18", 78077248);
        chromSizes.put("20", 63025520);
        chromSizes.put("Y", 59373566);
        chromSizes.put("19", 59128983);
        chromSizes.put("22", 51304566);
        chromSizes.put("21", 48129895);

    }


    public CollapseIpcrParameters(String[] args) throws IOException, ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(CollapseIpcrParameters.getOptions(), args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            MakeIpcrFileParameters.printHelp();
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

        boolean zipped = cmd.hasOption("z");
        noHeader = cmd.hasOption("n");

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

            default:
                LOGGER.error("Invalid output type -t");
                printHelp();
                exit(1);
        }

    }

    public boolean isNoHeader() {
        return noHeader;
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

    public static int getChromSize(String chrom) {
        return chromSizes.get(chrom);
    }

    public static Map<String, Integer> getChromSizes() {
        return chromSizes;
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
