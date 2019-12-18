package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrParseException;
import nl.umcg.suresnp.pipeline.tools.parameters.*;
import nl.umcg.suresnp.pipeline.tools.runners.*;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;

import java.io.IOException;

import static java.lang.System.exit;

public class IpcrTools {

    private static Logger LOGGER = Logger.getLogger(IpcrTools.class);

    public static void main(String[] args) {
        try {

            // TODO: proper javadoc and readme
            // Parse commandline arguments
            IpcrToolParameters params = new IpcrToolParameters(args);

            // Select which tool to run
            switch (params.getToolType()) {
                case "AssignVariantAlleles":
                    AssignVariantAllelesParameters varParams = new AssignVariantAllelesParameters(args);
                    AssignVariantAlleles assignVariantAlleles = new AssignVariantAlleles(varParams);
                    assignVariantAlleles.run();
                    break;
                case "GenerateBarcodeComplexityCurve":
                    GenerateBarcodeComplexityCurveParameters barcodeCurveParams = new GenerateBarcodeComplexityCurveParameters(args);
                    GenerateBarcodeComplexityCurve generateBarcodeComplexityCurve = new GenerateBarcodeComplexityCurve(barcodeCurveParams);
                    generateBarcodeComplexityCurve.run();
                    break;
                case "MakeIpcrFile":
                    MakeIpcrFileParameters barcodeCountParams = new MakeIpcrFileParameters(args);
                    MakeIpcrFile makeIpcrFile = new MakeIpcrFile(barcodeCountParams);
                    makeIpcrFile.run();
                    break;
                case "NormalizeCdnaWithIpcr":
                    CollapseIpcrParameters normalizeParams = new CollapseIpcrParameters(args);
                    CollapseIpcr collapseIpcr = new CollapseIpcr(normalizeParams);
                    collapseIpcr.run();
                    break;
                case "MakeReadNucleotideDistribution":
                    MakeReadNucleotideDistributionParameters nucParams = new MakeReadNucleotideDistributionParameters(args);
                    MakeReadNucleotideDistribution nucDist = new MakeReadNucleotideDistribution(nucParams);
                    nucDist.run();
                    break;
                default:
                    LOGGER.error("Did not supply a valid tooltype");
                    IpcrToolParameters.printHelp();
                    exit(1);
            }

        } catch (UnrecognizedOptionException e) {
            IpcrToolParameters.printHelp();
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
