package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class IpcrTools {

    private static Logger LOGGER = Logger.getLogger(IpcrTools.class);

    public static void main(String[] args) {
        try {

            // TODO: proper javadoc and readme
            // Parse commandline arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(IpcrToolsParameters.getOptions(), args);

            // Print help and exit
            if (cmd.hasOption("h")) {
                IpcrToolsParameters.printHelp();
                exit(0);
            }

            if (!cmd.hasOption("T")) {
                LOGGER.error("Missing required option -T");
                IpcrToolsParameters.printHelp();
                exit(1);
            }

            // Define the output writer, either stdout or to file
            IpcrOutputWriter outputWriter;

            if (cmd.hasOption("s")) {
                // When writing to stdout do not use log4j unless there is an error
                if (cmd.hasOption("r")) {
                    outputWriter = new SimpleIpcrStdoutWriter();
                } else {
                    outputWriter = new IpcrStdoutWriter();
                }
                Logger.getRootLogger().setLevel(Level.ERROR);

            } else {
                // When writing to a file check if the correct options are specified
                if (!cmd.hasOption("o")) {
                    LOGGER.error("-o not specified");
                    IpcrToolsParameters.printHelp();
                    exit(1);
                }

                boolean zipped = false;
                String extension = "";
                if (cmd.hasOption("z")) {
                    zipped = true;
                    extension = ".gz";
                }

                if (cmd.hasOption("r")) {
                    outputWriter = new SimpleIpcrOutputFileWriter(new File(cmd.getOptionValue("o").trim() + ".reduced.ipcr" + extension), zipped);
                } else {
                    outputWriter = new IpcrOutputFileWriter(new File(cmd.getOptionValue("o").trim() + ".full.ipcr" + extension), zipped);
                }
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
                    IpcrToolsParameters.printHelp();
                    exit(1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IpcrParseException e) {
            e.printStackTrace();
        }

    }
}
