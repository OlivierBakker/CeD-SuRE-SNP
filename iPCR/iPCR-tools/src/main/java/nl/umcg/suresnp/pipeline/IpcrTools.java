package nl.umcg.suresnp.pipeline;

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
                case "MergeBamWithBarcodeCounts":
                    MergeBamWithBarcodeCountsParameters barcodeCountParams = new MergeBamWithBarcodeCountsParameters(args);
                    MergeBamWithBarcodeCounts mergeBamWithBarcodeCounts = new MergeBamWithBarcodeCounts(barcodeCountParams);
                    mergeBamWithBarcodeCounts.run();
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
