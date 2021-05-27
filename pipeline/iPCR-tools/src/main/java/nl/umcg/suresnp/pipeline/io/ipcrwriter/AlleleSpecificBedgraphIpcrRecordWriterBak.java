package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.IpcrTools;
import nl.umcg.suresnp.pipeline.io.bedwriter.FourColBedWriter;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificIpcrRecord;
import nl.umcg.suresnp.pipeline.utils.EfficientBinnedDensityGenomicPileup;
import nl.umcg.suresnp.pipeline.utils.GenomicRegionPileup;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Deprecated
public class AlleleSpecificBedgraphIpcrRecordWriterBak implements AlleleSpecificIpcrOutputWriter {

    private static final Logger LOGGER = Logger.getLogger(AlleleSpecificBedgraphIpcrRecordWriterBak.class);
    private int binWidth;
    private String cdnaSampleToWrite;
    private List<AlleleSpecificIpcrRecord> recordBuffer;
    private FourColBedWriter alleleOneWriter;
    private FourColBedWriter alleleTwoWriter;

    public AlleleSpecificBedgraphIpcrRecordWriterBak(File output, boolean isZipped, String cdnaSampleToWrite, int binWidth) throws IOException {
        this.cdnaSampleToWrite = cdnaSampleToWrite;
        this.binWidth = binWidth;
        this.recordBuffer = new TreeList<>();
        alleleOneWriter = new FourColBedWriter(new File(output + "_A1"), isZipped, FileExtensions.BEDGRAPH);
        alleleTwoWriter = new FourColBedWriter(new File(output + "_A2"), isZipped, FileExtensions.BEDGRAPH);
    }

    @Override
    public void writeRecord(AlleleSpecificIpcrRecord record) throws IOException {
        writeRecord(record, "");

    }

    @Override
    public void writeRecord(AlleleSpecificIpcrRecord record, String reason) throws IOException {
        recordBuffer.add(record);
    }

    @Override
    public void writeHeader() throws IOException {

    }

    @Override
    public void writeHeader(String reason) throws IOException {

    }

    @Override
    public void flushAndClose() throws IOException {

        // Sort the buffer
        LOGGER.info("Sorting the record buffer");
        recordBuffer.sort(Comparator
                .comparing(AlleleSpecificIpcrRecord::getVariantIdentifier)
                .thenComparing(AlleleSpecificIpcrRecord::getReadAllele)
                .thenComparing(AlleleSpecificIpcrRecord::getStart));
        LOGGER.info("Done sorting the record buffer with " + recordBuffer.size() + " records");

        LOGGER.info("Generating genomic bins for both alleles");
        GenomicRegionPileup pileupA1 = null;// new EfficientBinnedDensityGenomicPileup(binWidth, cdnaSampleToWrite);
        GenomicRegionPileup pileupA2 = null;//new EfficientBinnedDensityGenomicPileup(binWidth, cdnaSampleToWrite);
        LOGGER.info("Done generating genomic bins for both alleles");

        // Assign the read to either the A1 or A2 pileup
        String cachedVariant = null;
        String cachedAllele = null;
        int i = 0;
        for (AlleleSpecificIpcrRecord curRec : recordBuffer) {
            IpcrTools.logProgress(i, 1000000, "AlleleSpecificBedgraphIpcrRecordWriter");

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

            i++;
        }

        // Flush progressbar
        System.out.println();
        LOGGER.info("Done generating pileups");

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

    private void writeRecordToOuputStream() {

    }
}
