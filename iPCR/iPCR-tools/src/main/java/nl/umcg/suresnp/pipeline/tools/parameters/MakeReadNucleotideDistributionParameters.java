package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import static java.lang.System.exit;

public class MakeReadNucleotideDistributionParameters {


    private final Logger LOGGER = Logger.getLogger(MakeReadNucleotideDistributionParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String inputBam;
    private GenericFile inputIpcr;
    private String outputPrefix;
    private GenericFile regionFilterFile;

    // General arguments
    private String toolType;

    private int barcodeCountFiler;

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
                .longOpt("input-bam")
                .hasArg(true)
                .desc("Input bamfile")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("i")
                .longOpt("input-ipcr")
                .hasArg(true)
                .desc("The IPCR file to use")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("d")
                .longOpt("bc-filter")
                .hasArg(true)
                .desc("# of cDNA barcodes required to be included, defaults to 1")
                .argName("<integer>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("f")
                .longOpt("region-filter")
                .hasArg(true)
                .desc("BED file to only output records overlapping those regions")
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

    public MakeReadNucleotideDistributionParameters(String[] args) throws ParseException {

        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            printHelp();
            exit(0);
        }

        // Input files
        inputBam = cmd.getOptionValue("b").trim();
        inputIpcr = new GenericFile(cmd.getOptionValue('i').trim());

        toolType = "MakeReadNucleotideDistribution";

        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            outputPrefix = "ipcrtools";
        }

        if (cmd.hasOption("f")) {
            regionFilterFile = new GenericFile(cmd.getOptionValue("f"));
        }

        if (cmd.hasOption('d')) {
            barcodeCountFiler = Integer.parseInt(cmd.getOptionValue('d'));
        } else {
            barcodeCountFiler = 1;
        }
    }

    public int getBarcodeCountFilter() {
        return barcodeCountFiler;
    }

    public String getInputBam() {
        return inputBam;
    }

    public GenericFile getInputIpcr() {
        return inputIpcr;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public GenericFile getRegionFilterFile() {
        return regionFilterFile;
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
