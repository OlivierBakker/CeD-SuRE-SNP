package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.icpr.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.io.icpr.IpcrParseException;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;

import java.io.IOException;

public class IpcrTools {

    private static Logger LOGGER = Logger.getLogger(IpcrTools.class);

    public static void main(String[] args) {
        try {

            // TODO: proper javadoc and readme
            // Parse commandline arguments
            IpcrToolsParameters params = new IpcrToolsParameters(args);
            IpcrOutputWriter outputWriter = params.getOutputWriter();

            // Select which tool to run
            switch (params.getToolType()) {
                case "MergeBamWithBarcodes":
                    MergeBamWithBarcodes.run(params, outputWriter);
                    break;
                case "AssignVariantAlleles":
                    AssignVariantAlleles curTool = new AssignVariantAlleles(params);
                    curTool.run(params);
                    break;
            }

        } catch (UnrecognizedOptionException e) {
            IpcrToolsParameters.printHelp();
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IpcrParseException e) {
            e.printStackTrace();
        }

    }
}
