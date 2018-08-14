package nl.umcg.suresnp.pipeline;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MergeBamWithBarcodesParameters {

    private static final Options OPTIONS;

    static {
        OPTIONS = new Options();
        Option option;

        option = Option.builder("i")
                .longOpt("input-bam")
                .hasArg(true)
                .required()
                .desc("Input bamfile")
                .argName("path/to/file")
                .build();

        OPTIONS.addOption(option);

        option = Option.builder("b")
                .longOpt("barcode-info")
                .hasArg(true)
                .required()
                .desc("The file containing read names and barcodes")
                .argName("path/to/file")
                .build();


        OPTIONS.addOption(option);

        option = Option.builder("o")
                .longOpt("output")
                .hasArg(true)
                .required()
                .desc("Output prefix")
                .argName("path/to/output")
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
