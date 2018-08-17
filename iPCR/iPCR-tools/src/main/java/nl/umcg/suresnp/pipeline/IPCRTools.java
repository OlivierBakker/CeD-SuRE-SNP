package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.IPCROutputFileWriter;
import nl.umcg.suresnp.pipeline.io.IPCROutputWriter;
import nl.umcg.suresnp.pipeline.io.IPCRStdoutWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class IPCRTools {

    private static Logger LOGGER = Logger.getLogger(IPCRTools.class);

    public static void main(String[] args) {
        try {

            // TODO: proper javadoc and readme
            // Parse commandline arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(IPCRToolsParameters.getOptions(), args);

            // Define the output writer, either stdout or to file
            IPCROutputWriter outputWriter;

            if (cmd.hasOption("s")) {
                // When writing to stdout do not use log4j unless there is an error
                outputWriter = new IPCRStdoutWriter();
                Logger.getRootLogger().setLevel(Level.ERROR);
            } else {
                // When writing to a file check if the correct options are specified
                if (!cmd.hasOption("o")) {
                    LOGGER.error("-o not specified");
                    IPCRToolsParameters.printHelp();
                    exit(1);
                }
                outputWriter = new IPCROutputFileWriter(new File(cmd.getOptionValue("o").trim()), false);
            }

            // Select which tool to run
            switch (cmd.getOptionValue("T").trim()) {
                case "MergeBamWithBarcodes":
                    MergeBamWithBarcodes.run(cmd, outputWriter);
                    break;
                case "AddAlleleInfo":
                    AddAlleleInfo.run(cmd, outputWriter);
                    break;
                default:
                    IPCRToolsParameters.printHelp();
                    exit(1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
