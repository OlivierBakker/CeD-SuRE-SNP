package nl.umcg.suresnp.pipeline;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class IPCRToolsParameters {

    private static final Options OPTIONS;

    static {
        OPTIONS = new Options();
        Option option;

        option = Option.builder("i")
                .longOpt("input-bam")
                .hasArg(true)
                .required()
                .desc("Input bamfile SORTED ON QUERY NAME!!!!!")
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
                .desc("Output prefix")
                .argName("path/to/output")
                .build();


        OPTIONS.addOption(option);

        option = Option.builder("s")
                .longOpt("stdout")
                .desc("Pipe output to stdout. Will omit logging")
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
