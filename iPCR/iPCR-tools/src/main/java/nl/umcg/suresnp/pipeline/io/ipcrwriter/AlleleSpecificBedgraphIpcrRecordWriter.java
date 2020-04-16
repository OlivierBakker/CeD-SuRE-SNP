package nl.umcg.suresnp.pipeline.io.ipcrwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.bedwriter.FourColBedWriter;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.AlleleSpecificIpcrRecord;
import nl.umcg.suresnp.pipeline.utils.GenomicRegionPileup;
import org.apache.commons.collections4.list.TreeList;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class AlleleSpecificBedgraphIpcrRecordWriter implements AlleleSpecificIpcrOutputWriter {


    private int binWidth;
    private String cdnaSampleToWrite;
    private List<AlleleSpecificIpcrRecord> recordBuffer;
    private FourColBedWriter alleleOneWriter;
    private FourColBedWriter alleleTwoWriter;

    public AlleleSpecificBedgraphIpcrRecordWriter(File output, boolean isZipped, String cdnaSampleToWrite, int binWidth) throws IOException {
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
        recordBuffer.sort(Comparator
                .comparing(AlleleSpecificIpcrRecord::getContig)
                .thenComparing(AlleleSpecificIpcrRecord::getStart)
                .thenComparing(AlleleSpecificIpcrRecord::getReadAllele));

        GenomicRegionPileup pileupA1 = new GenomicRegionPileup(binWidth, cdnaSampleToWrite);
        GenomicRegionPileup pileupA2 = new GenomicRegionPileup(binWidth, cdnaSampleToWrite);

        String cachedVariant = null;
        String cachedAllele = null;
        for (AlleleSpecificIpcrRecord curRec : recordBuffer) {
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

        for (BedRecord rec : pileupA1.getPileup()) {
            alleleOneWriter.writeRecord(rec);
        }

        for (BedRecord rec : pileupA2.getPileup()) {
            alleleTwoWriter.writeRecord(rec);
        }

        alleleOneWriter.flushAndClose();
        alleleTwoWriter.flushAndClose();

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
