package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.*;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.IpcrRecordFilterType;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class RecodeParameters {

    private final Logger LOGGER = Logger.getLogger(RecodeParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String[] inputIpcr;
    private String[] inputCdna;

    // Yes, should be an enum, but I couldn't be bothered
    private String inputType;
    private boolean replaceOldCdnaSamples;
    private GenericFile regionFilterFile;
    private List<IpcrRecordFilter> filters;

    // Output
    private String outputPrefix;
    private String outputType;
    private IpcrOutputWriter outputWriter;
    private boolean isStdoutput;
    private String sampleToWrite;

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

        option = Option.builder("k")
                .longOpt("input-type")
                .hasArg(true)
                .desc("The input type")
                .argName("IPCR|IPCR_INDEXED")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("t")
                .longOpt("output-type")
                .hasArg(true)
                .desc("The output type")
                .argName("IPCR|IPCR_INDEXED|MACS|BED|BEDPE")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("rf")
                .longOpt("region-filter")
                .hasArg(true)
                .desc("BED file to only output records overlapping those regions")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("f")
                .longOpt("filter")
                .hasArg(true)
                .desc("One of the following filters: IN_REGION | ANY_BC_GT_EQ | ANY_BC_ST_EQ\n" +
                        "For more details see documentation")
                .argName("-f <filtername>;<args>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("b")
                .longOpt("input-barcodes")
                .hasArg(true)
                .desc("Barcode counts to add to the iPCR file")
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
                .longOpt("sample-to-write")
                .hasArg(true)
                .desc("cDNA sample to use to write MACS output. If omitted will write on bedfile for all of them")
                .argName("sample")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("z")
                .longOpt("zipped")
                .hasArg(false)
                .desc("Should output be zipped? If using -t IPCR_INDEXED this is ignored as tabix needs bgzipped output.")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("r")
                .longOpt("replace-cdna-samples")
                .desc("Replace old cDNA samples with new ones instead of adding them")
                .hasArg(false)
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("u")
                .longOpt("dont-write-ipcr-with-macs")
                .hasArg(false)
                .desc("Do not write the ipcr track for MACS output. Can be used when writing multiple samples.")
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
            printHelp();
            exit(0);
        }

        // Input iPCR files
        if (cmd.hasOption('i')) {
            inputIpcr = cmd.getOptionValues('i');
        }

        // Input barcode count files
        if (cmd.hasOption('b')) {
            inputCdna = cmd.getOptionValues('b');
        }

        // BED file containing regions to include
        if (cmd.hasOption("rf")) {
            regionFilterFile = new GenericFile(cmd.getOptionValue("rf"));
        }

        // Define and parse filters
        if (cmd.hasOption("f")) {
            filters = new ArrayList<>();
            for (String curFilter : cmd.getOptionValues("f")) {
                filters.add(IpcrRecordFilterType.createFilter(curFilter));
            }
        }

        // Input type
        if (cmd.hasOption('k')) {
            inputType = cmd.getOptionValue('k');
            switch (inputType) {
                case "IPCR":
                case "IPCR_INDEXED":
                    break;
                default:
                    LOGGER.error("Invalid input type, must be either IPCR");
                    printHelp();
                    exit(-1);
            }
        } else {
            inputType = "IPCR";
        }

        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            LOGGER.error("-o not specified");
            printHelp();
            exit(1);
        }

        if (cmd.hasOption('t')) {
            outputType = cmd.getOptionValue('t').trim();
        } else {
            outputType = "IPCR";
        }

        if (cmd.hasOption('s')) {
            sampleToWrite = cmd.getOptionValue('s').trim();
        } else {
            sampleToWrite = null;
        }

        replaceOldCdnaSamples = cmd.hasOption("r");
        boolean writeIpcr = !cmd.hasOption("u");
        boolean zipped = cmd.hasOption("z");

        switch (outputType) {
            case "BEDPE":
                if (inputCdna != null) {
                    outputWriter = new BedpeIpcrRecordWriter(new File(outputPrefix), zipped, inputCdna);
                } else {
                    outputWriter = new BedpeIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "BED":
                if (inputCdna != null) {
                    outputWriter = new BedIpcrRecordWriter(new File(outputPrefix), zipped, inputCdna);
                } else {
                    outputWriter = new BedIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "IPCR":
                if (inputCdna != null) {
                    outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix), zipped, inputCdna);
                } else {
                    outputWriter = new GenericIpcrRecordWriter(new File(outputPrefix), zipped);
                }
                break;
            case "IPCR_INDEXED":
                if (inputCdna != null) {
                    outputWriter = new BlockCompressedIpcrRecordWriter(outputPrefix, inputCdna);
                } else {
                    outputWriter = new BlockCompressedIpcrRecordWriter(outputPrefix);
                }
                break;
            case "MACS":
                if (sampleToWrite == null) {
                    LOGGER.info("No sample provided but output type is MACS. Will output bedfiles for all samples");
                }

                if (inputCdna != null) {
                    outputWriter = new MacsIpcrRecordWriter(new File(outputPrefix), zipped, inputCdna, sampleToWrite, writeIpcr);
                } else {
                    outputWriter = new MacsIpcrRecordWriter(new File(outputPrefix), zipped, sampleToWrite, writeIpcr);
                }
                break;
            default:
                LOGGER.error("Invalid output type -t");
                printHelp();
                exit(1);
        }

        toolType = "Recode";
    }

    public List<IpcrRecordFilter> getFilters() {
        return filters;
    }

    public GenericFile getRegionFilterFile() {
        return regionFilterFile;
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

    public String[] getInputCdna() {
        return inputCdna;
    }

    public String getToolType() {
        return toolType;
    }

    public static Options getOPTIONS() {
        return OPTIONS;
    }

    public boolean isReplaceOldCdnaSamples() {
        return replaceOldCdnaSamples;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }

}
