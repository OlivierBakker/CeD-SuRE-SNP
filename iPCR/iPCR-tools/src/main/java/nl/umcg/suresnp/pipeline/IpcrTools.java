package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrParseException;
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
                case "MergeSamWithBarcodes":
                    MergeBamWithBarcodesParameters barcodeCountParams = new MergeBamWithBarcodesParameters(args);
                    MergeBamWithBarcodes mergeBamWithBarcodes = new MergeBamWithBarcodes(barcodeCountParams);
                    mergeBamWithBarcodes.run();
                    break;
                case "NormalizeCdnaWithIpcr":
                    NormalizeCdnaWithIpcrParameters normalizeParams = new NormalizeCdnaWithIpcrParameters();
                    NormalizeCdnaWithIpcr normalizeCdnaWithIpcr = new NormalizeCdnaWithIpcr();
                    normalizeCdnaWithIpcr.run(args);
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
