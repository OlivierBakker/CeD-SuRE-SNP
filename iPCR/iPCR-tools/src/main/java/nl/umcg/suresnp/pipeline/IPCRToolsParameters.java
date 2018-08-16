package nl.umcg.suresnp.pipeline;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class IPCRToolsParameters {

    private static final Options OPTIONS;

    static {
        OPTIONS = new Options();
        Option option;

        option = Option.builder("T")
                .longOpt("tool")
                .hasArg(true)
                .required()
                .desc("The tool to apply:\n" +
                        "- MergeWithBarcodes" +
                        "- AddAlleleInfo")
                .argName("path/to/file")
                .build();

        OPTIONS.addOption(option);

        option = Option.builder("i")
                .longOpt("input-bam")
                .hasArg(true)
                .desc("Input bamfile SORTED ON QUERY NAME!!!!!")
                .argName("path/to/file")
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
                .desc("Pipe output to stdout. Will omit logging")
                .build();

        OPTIONS.addOption(option);

        option = Option.builder("g")
                .longOpt("input-genotype")
                .hasArg(true)
                .desc("Currently only supports VCF")
                .build();

        OPTIONS.addOption(option);

        option = Option.builder("p")
                .longOpt("input-ipcr-file")
                .hasArg(true)
                .desc("Input from MergeBamWithBarcodes")
                .build();

        OPTIONS.addOption(option);

    }

    public static Options getOptions(){
        return OPTIONS;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }
}
