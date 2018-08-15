package nl.umcg.suresnp.pipeline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;


import static java.lang.System.exit;

public class IPCRTools {

    private static Logger LOGGER = Logger.getLogger(IPCRTools.class);

    public static void main(String[] args) {
        try {

            // Parse commandline arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(IPCRToolsParameters.getOptions(), args);

            switch (cmd.getOptionValue("T").trim()) {
                case "MergeBamWithBarcodes":
                    MergeBamWithBarcodes.run(cmd);
                    break;
                default:
                    IPCRToolsParameters.printHelp();
                    exit(1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
