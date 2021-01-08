package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.bedwriter.FourColBedWriter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AdaptableScoreProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificIpcrRecord;
import nl.umcg.suresnp.pipeline.utils.EfficientBinnedDensityGenomicPileup;
import nl.umcg.suresnp.pipeline.utils.GenomicRegionPileup;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class AlleleSpecificBedgraphIpcrRecordWriter implements AlleleSpecificIpcrOutputWriter {

    private static final Logger LOGGER = Logger.getLogger(AlleleSpecificBedgraphIpcrRecordWriter.class);
    private FourColBedWriter alleleOneWriter;
    private FourColBedWriter alleleTwoWriter;

    private GenomicRegionPileup pileupA1;
    private GenomicRegionPileup pileupA2;

    private String cachedVariant = null;
    private String cachedAllele = null;

    public AlleleSpecificBedgraphIpcrRecordWriter(File output, boolean isZipped, AdaptableScoreProvider scoreProvider, int binWidth) throws IOException {

        alleleOneWriter = new FourColBedWriter(new File(output + "_A1"), isZipped, FileExtensions.BEDGRAPH);
        alleleTwoWriter = new FourColBedWriter(new File(output + "_A2"), isZipped, FileExtensions.BEDGRAPH);

        LOGGER.info("Generating genomic bins for both alleles");
        this.pileupA1 = new EfficientBinnedDensityGenomicPileup(binWidth, scoreProvider);
        this.pileupA2 = new EfficientBinnedDensityGenomicPileup(binWidth, scoreProvider);
        LOGGER.info("Done generating genomic bins for both alleles");

        LOGGER.info("Records must be provided sorted by variant to this writer");
    }

    @Override
    public void writeRecord(AlleleSpecificIpcrRecord record) throws IOException {
        // Records must be provided sorted by variant
        writeRecord(record, "");

    }

    @Override
    public void writeRecord(AlleleSpecificIpcrRecord curRec, String reason) throws IOException {
        // Records must be provided sorted by variant
        if (cachedVariant == null) {
            cachedVariant = curRec.getVariantIdentifier();
        }
        if (cachedAllele == null) {
            cachedAllele = curRec.getReadAllele();
        }

        if (cachedVariant.equals(curRec.getVariantIdentifier())) {
            if (cachedAllele.equals(curRec.getReadAllele())) {
                pileupA1.addIpcrRecord(curRec);
            } else {
                pileupA2.addIpcrRecord(curRec);
            }
        } else {
            cachedVariant = curRec.getVariantIdentifier();
            cachedAllele = curRec.getReadAllele();
            pileupA1.addIpcrRecord(curRec);
        }
    }

    @Override
    public void writeHeader() throws IOException {

    }

    @Override
    public void writeHeader(String reason) throws IOException {

    }

    @Override
    public void flushAndClose() throws IOException {

        LOGGER.info("Writing pileups");
        while(pileupA1.hasNext()) {
            alleleOneWriter.writeRecord(pileupA1.getNextRecord());
        }

        while(pileupA2.hasNext()) {
            alleleTwoWriter.writeRecord(pileupA2.getNextRecord());
        }

        alleleOneWriter.flushAndClose();
        alleleTwoWriter.flushAndClose();

        LOGGER.info("Done writing pileups");

    }

    @Override
    public String[] getBarcodeCountFilesSampleNames() {
        return new String[0];
    }

    @Override
    public void setBarcodeCountFilesSampleNames(String[] barcodeCountFilesSampleNames) {
    }
}
