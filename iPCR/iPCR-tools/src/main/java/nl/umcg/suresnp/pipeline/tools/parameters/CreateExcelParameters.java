package nl.umcg.suresnp.pipeline.tools.parameters;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.*;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.SampleSumScoreProvider;
import nl.umcg.suresnp.pipeline.records.summarystatistic.VariantBasedNumericGenomicAnnotation;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class CreateExcelParameters {

    private final Logger LOGGER = Logger.getLogger(CreateExcelParameters.class);
    private final CommandLine cmd;

    private String outputPrefix;

    private File inputVcf;
    private GenericFile regionFilterFile;

    private List<VariantBasedNumericGenomicAnnotation> variantAnnotationFiles;
    private List<GenericGenomicAnnotation> regionAnnotationFiles;

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
                .longOpt("input-vcf")
                .hasArg(true)
                .desc("Input tabix indexed vcf files with the variants to annotate")
                .argName("path/to/file")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("rf")
                .longOpt("region-filter")
                .hasArg(true)
                .desc("BED formatted file with regions to include")
                .argName("path/to/file.bed")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("v")
                .longOpt("variant-annot")
                .hasArg(true)
                .desc("File with variant info to annotate with. First column must be rsIDs. " +
                        "Subseqeunt columns must be numeric. " +
                        "To annotate all sheets this file use \"AllSheets\" as the name")
                .argName("<name>=<path/to/file[.*|.bgz]>")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("r")
                .longOpt("region-annot")
                .hasArg(true)
                .desc("3 + n col BED (optionally tabixed with .bgz) with region info to annotate with. " +
                        "Subsequent columns will be interpreted as strings. " +
                        "To annotate all sheets with this file use \"AllSheets\" as the name")
                .argName("<name>=<path/to/file>")
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

    public CreateExcelParameters(String[] args) throws IOException, ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(OPTIONS, args);

        // Print help and exit
        if (cmd.hasOption("h")) {
            printHelp();
            exit(0);
        }

        inputVcf = new File(cmd.getOptionValue("i").trim());


        if (cmd.hasOption("rf")) {
            regionFilterFile = new GenericFile(cmd.getOptionValue("rf").trim());
        }

        if (cmd.hasOption("r")) {
            String[] curFiles = cmd.getOptionValues("r");
            regionAnnotationFiles = new ArrayList<>(curFiles.length);
            for (String file: curFiles) {
                String[] params = file.split("=");
                if (params.length != 2) {
                    throw new IllegalArgumentException("Annotations not provided in proper manner");
                }
                regionAnnotationFiles.add(new GenericGenomicAnnotation(new GenericFile(params[1]), params[0]));
            }
        }

        if (cmd.hasOption("v")) {
            String[] curFiles = cmd.getOptionValues("v");
            variantAnnotationFiles = new ArrayList<>(curFiles.length);

            for (String file: curFiles) {
                String[] params = file.split("=");
                if (params.length != 2) {
                    throw new IllegalArgumentException("Annotations not provided in proper manner");
                }
                variantAnnotationFiles.add(new VariantBasedNumericGenomicAnnotation(new GenericFile(params[1]), params[0]));
            }
        }


        // Define the output writer, either stdout or to file
        if (cmd.hasOption('o')) {
            outputPrefix = cmd.getOptionValue("o").trim();
        } else {
            outputPrefix = "ipcrtools";
        }

    }


    public CommandLine getCmd() {
        return cmd;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public void setOutputPrefix(String outputPrefix) {
        this.outputPrefix = outputPrefix;
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

    public File getInputVcf() {
        return inputVcf;
    }

    public GenericFile getRegionFilterFile() {
        return regionFilterFile;
    }

    public List<VariantBasedNumericGenomicAnnotation> getVariantAnnotationFiles() {
        return variantAnnotationFiles;
    }

    public List<GenericGenomicAnnotation> getRegionAnnotationFiles() {
        return regionAnnotationFiles;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }
}
