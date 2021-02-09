package nl.umcg.suresnp.pipeline;

import com.itextpdf.text.DocumentException;
import nl.umcg.suresnp.pipeline.tools.parameters.*;
import nl.umcg.suresnp.pipeline.tools.runners.*;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                    AssignVariantAlleles assignVariantAlleles = new AssignVariantAlleles(new AssignVariantAllelesParameters(args));
                    assignVariantAlleles.run();
                    break;
                case "MakeBarcodeComplexityCurve":
                    MakeBarcodeComplexityCurve makeBarcodeComplexityCurve = new MakeBarcodeComplexityCurve(new MakeBarcodeComplexityCurveParameters(args));
                    makeBarcodeComplexityCurve.run();
                    break;
                case "MakeIpcrFile":
                    MakeIpcrFile makeIpcrFile = new MakeIpcrFile(new MakeIpcrFileParameters(args));
                    makeIpcrFile.run();
                    break;
                case "CollapseIpcr":
                    CollapseIpcr collapseIpcr = new CollapseIpcr(new CollapseIpcrParameters(args));
                    collapseIpcr.run();
                    break;
                case "MakeReadNucleotideDistribution":
                    MakeReadNucleotideDistribution nucDist = new MakeReadNucleotideDistribution(new MakeReadNucleotideDistributionParameters(args));
                    nucDist.run();
                    break;
                case "MakeBarcodeCounts":
                    MakeBarcodeCounts makeBarcodeCounts = new MakeBarcodeCounts(new MakeBarcodeCountsParameters(args));
                    makeBarcodeCounts.run();
                    break;
                case "MakeBarcodeStats":
                    SureSnpUtils makeBcStats = new SureSnpUtils(new SureSnpUtilsParameters(args));
                    makeBcStats.barcodeOverlap();
                    break;
                case "GetInsertSizes":
                    SureSnpUtils getInsertSizes = new SureSnpUtils(new SureSnpUtilsParameters(args));
                    getInsertSizes.getInsertSizes();
                    break;
                case "MakeBarcodeCountHist":
                    SureSnpUtils barcodeCountHist = new SureSnpUtils(new SureSnpUtilsParameters(args));
                    barcodeCountHist.makeBarcodeCountHist();
                    break;
                case "GetCdnaCorrelations":
                    SureSnpUtils cdnaCorrelations = new SureSnpUtils(new SureSnpUtilsParameters(args));
                    cdnaCorrelations.getCdnaCorrelations();
                    break;
                case "GetPeakCorrelations":
                    PeakUtils correlatePeaks = new PeakUtils(new PeakUtilsParameters(args));
                    correlatePeaks.getPeakCorrelations();
                    break;
                case "OverlapPeaks":
                    PeakUtils overlapPeaks = new PeakUtils(new PeakUtilsParameters(args));
                    overlapPeaks.createConsensusPeaks();
                    break;
                case "CollapseEncodeTfbs":
                    PeakUtils collapseEncode = new PeakUtils(new PeakUtilsParameters(args));
                    collapseEncode.collapseEncodeChipSeq();
                    break;
                case "Recode":
                    Recode recode = new Recode(new RecodeParameters(args));
                    recode.run();
                    break;
                case "SubsetBam":
                    SubsetBam subsetBam = new SubsetBam(new SubsetBamParameters(args));
                    subsetBam.run();
                    break;
                case "IndexIpcr":
                    SureSnpUtils index = new SureSnpUtils(new SureSnpUtilsParameters(args));
                    index.indexIpcr();
                    break;
                case "CreateExcel":
                    CreateExcel createExcel = new CreateExcel(new CreateExcelParameters(args));
                    createExcel.run();
                    break;
                case "GetPeakCounts":
                    PeakUtils getPeakCounts = new PeakUtils(new PeakUtilsParameters(args));
                    getPeakCounts.getPeakCountMatrix();
                    break;
                case "GenomicRegionEnrichment":
                    GenomicRegionEnrichment genomicRegionEnrichment = new GenomicRegionEnrichment(new GenomicRegionEnrichmentParameters(args));
                    genomicRegionEnrichment.run();
                    break;
                default:
                    LOGGER.error("Did not supply a valid tooltype");
                    IpcrToolParameters.printHelp();
                    exit(1);
            }

        } catch (UnrecognizedOptionException | IllegalArgumentException e) {
            IpcrToolParameters.printHelp();
            e.printStackTrace();
        } catch (ParseException | IOException | java.text.ParseException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void logProgress(long curCount, long interval, String classname) {
        logProgress(curCount, interval, classname, "million");
    }

    public static void logProgress(long curCount, long interval, String classname, String unit) {
        if (curCount > 0) {
            if (curCount % interval == 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");
                System.out.print(formatter.format(new Date()) + " INFO  [" + classname + "] Processed " + curCount / interval + " " + unit + " records\r");
            }
        }
    }
}
