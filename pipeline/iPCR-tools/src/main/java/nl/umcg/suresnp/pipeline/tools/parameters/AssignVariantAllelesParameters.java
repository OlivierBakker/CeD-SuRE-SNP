package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.ipcrwriter.AlleleSpecificBedgraphIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.AlleleSpecificIpcrOutputWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.AlleleSpecificIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.MinimalAlleleSpecificIpcrRecordWriter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.NormalizedSampleScoreProvider;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class AssignVariantAllelesParameters {

    private final Logger LOGGER = Logger.getLogger(MakeBarcodeComplexityCurveParameters.class);
    private final CommandLine cmd;

    // IO arguments
    private String[] inputBam;
    private String[] inputIpcr;
    private String[] secondaryInputBam;
    private String inputVcf;

    private String outputPrefix;
    private String outputSuffix;
    private String outputType;
    private AlleleSpecificIpcrOutputWriter outputWriter;

    // General arguments
    private String toolType;

    // Tool specific arguments
    private String sampleGenotypeId;
    private String[] cDNASamples;
    private String[] variantsToInclude;

    private boolean primaryBamOut;

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

        option = Option.builder("j")
                .longOpt("secondary-input-bam")
                .hasArg(true)
                .desc("Secondary input bamfile, Used to assign reads when not in primary bam file." +
                        "Primary bamfile has higher priority, so in case of a duplicate, the primary is used." +
                        "This is used for assigning reads from homzoygous reference samples which are not in the --bamout file")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("g")
                .longOpt("input-genotype")
                .hasArg(true)
                .desc("Currently only supports VCF")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("v")
                .longOpt("use-sample-genotype")
                .hasArg(true)
                .desc("The sample identifier to use to check the read alleles. If the call is HOM_REF only alleles " +
                        "that match the reference will be assigned. If the call is HOM_ALT only reads matching the " +
                        "alt allele will be assigned. Mismatching reads will be written to <prefix>.discarded.reads.txt")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("b")
                .longOpt("input-ipcr-files")
                .hasArg(true)
                .desc("One or more BGZipped and indexed iPCR file")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("t")
                .longOpt("output-type")
                .hasArg(true)
                .desc("Output type")
                .argName("FULL | MINIMAL | BEDGRAPH")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("c")
                .longOpt("cDNA-samples")
                .hasArg(true)
                .desc("One or more sample identifiers to sum the cDNA scores for. For IPCR use provide the string IPCR as the first item, if provided cDNA is ignored")
                .argName("<sampleid> [-c <sampleid>]")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("vf")
                .longOpt("variant-filter")
                .hasArg(true)
                .desc("One or more variant id's")
                .argName("<variant id> [-vf <variant id>]")
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

        option = Option.builder("r")
                .longOpt("reduced")
                .desc("Print reduced output (without the sequences, cigars and variant info. " +
                        "Variant info is written to a separate file. Can be used to save space when using a large library)")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("z")
                .longOpt("zipped")
                .hasArg(false)
                .desc("Should output be zipped?")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("bo")
                .longOpt("bam-out")
                .hasArg(false)
                .desc("Should the bam be written, only usefull for debugging and when using -vf")
                .build();
        OPTIONS.addOption(option);

    }

    public AssignVariantAllelesParameters(String[] args) throws ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            printHelp();
            exit(0);
        }

        primaryBamOut = cmd.hasOption("bo");

        // Input files
        inputBam = cmd.getOptionValues("i");
        inputVcf = cmd.getOptionValue("g").trim();
        inputIpcr = cmd.getOptionValues('b');
        toolType = "AssignVariantAlleles";

        if (cmd.hasOption("j")) {
            secondaryInputBam = cmd.getOptionValues("j");
        }

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

        boolean zipped = false;
        outputSuffix = "";
        if (cmd.hasOption("z")) {
            zipped = true;
            outputSuffix = ".gz";
        }

        if (cmd.hasOption("t")) {
            outputType = cmd.getOptionValue("t");
        } else {
            outputType = "FULL";
        }

        if (cmd.hasOption("vf")) {
            variantsToInclude = cmd.getOptionValues("vf");
        }

        switch (outputType) {
            case "FULL":
                outputWriter = new AlleleSpecificIpcrRecordWriter(new File(outputPrefix + ".full.allele.specific.ipcr" + outputSuffix), zipped);
                break;
            case "MINIMAL":
                outputWriter = new MinimalAlleleSpecificIpcrRecordWriter(new File(outputPrefix + ".minimal.allele.specific.ipcr" + outputSuffix), zipped);
                break;
            case "BEDGRAPH":
                if (!cmd.hasOption("c")) {
                    LOGGER.error("-c not specified with BEDGRAPH output type. Need to know which sample(s) to score with");
                    printHelp();
                    exit(1);
                }
                cDNASamples = cmd.getOptionValues("c");
                AdaptableScoreProvider provider = new NormalizedSampleScoreProvider(cDNASamples);
                outputWriter = new AlleleSpecificBedgraphIpcrRecordWriter(new File(outputPrefix), zipped, provider, 1);
                break;
        }

        if (cmd.hasOption("v")) {
            sampleGenotypeId = cmd.getOptionValue('v').trim();
        }
    }

    public String[] getVariantsToInclude() {
        return variantsToInclude;
    }

    public String getOutputType() {
        return outputType;
    }

    public String getSampleGenotypeId() {
        return sampleGenotypeId;
    }

    public boolean hasSecondaryInputBam() {
        return secondaryInputBam != null;
    }

    public String[] getSecondaryInputBam() {
        return secondaryInputBam;
    }

    public String[] getInputBam() {
        return inputBam;
    }

    public CommandLine getCmd() {
        return cmd;
    }

    public String[] getInputIpcr() {
        return inputIpcr;
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

    public boolean isPrimaryBamOut() {
        return primaryBamOut;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }

}
