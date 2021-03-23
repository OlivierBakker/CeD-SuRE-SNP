package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.*;
import htsjdk.samtools.util.SortingCollection;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.SubsetBamParameters;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class SubsetBam {

    private static final Logger LOGGER = Logger.getLogger(SubsetBam.class);
    private SubsetBamParameters params;
    private int maxRecordsInMem = 100000000;

    // Filters PCR duplicates if using a iPCR file that has been collapsed with CollapseIpcr
    // Ensures the info used for peakcalling is the same as for the ASE analysis
    // Makes Picard MarkDuplicates step redundant as a unique barcode guarrantees a unique molecule
    public SubsetBam(SubsetBamParameters params) {
        this.params = params;
    }

    public void run() throws IOException, ParseException {
        LOGGER.warn("Make sure you have run CollapseIpcr first so that barcodes are unique when using this " +
                "as a replacement for Picard's MarkDuplicates");
        Set<String> ipcrUniqueReads = getIpcrReadNameSet();

        if (params.isSortAndIndex()) {
            subsetBamAndSort(ipcrUniqueReads);
        } else {
            subsetOnly(ipcrUniqueReads);
        }
    }

    private void subsetOnly(Set<String> ipcrUniqueReads) throws IOException {

        SamReader samReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT).open(new File(params.getInputBam()[0]));
        SAMFileWriter outputWriter = new SAMFileWriterFactory().makeSAMOrBAMWriter(samReader.getFileHeader(), true, new File(params.getOutputPrefix() + ".bam"));

        int i = 0;
        int recordsWritten = 0;
        for (SAMRecord record : samReader) {
            logProgress(i, 1000000, "SubsetBam");
            String name = record.getReadName();
            name = name.split(" ")[0];

            if (ipcrUniqueReads.contains(name)) {
                outputWriter.addAlignment(record);
                recordsWritten++;
            }

            i++;
        }

        samReader.close();
        outputWriter.close();

        LOGGER.info("Written " + recordsWritten + " overlapping records");
        LOGGER.info("iPCR input: " + ipcrUniqueReads.size() + " this is PE so /2");
        LOGGER.info("BAM total: " + i);
        LOGGER.info("% written: " + Math.round(((double) recordsWritten / (double) i) * 100));
    }


    private void subsetBamAndSort(Set<String> ipcrUniqueReads) throws IOException, ParseException {

        SamReader samReader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT).open(new File(params.getInputBam()[0]));
        SAMFileHeader outputHeader = samReader.getFileHeader();
        outputHeader.setSortOrder(SAMFileHeader.SortOrder.coordinate);

        // Create the soring collection. Can accept <maxRecordsInRam> records without spilling to disk
        SortingCollection<SAMRecord> testing = SortingCollection.newInstance(SAMRecord.class,
                new BAMRecordCodec(outputHeader),
                new SAMRecordCoordinateComparator(),
                maxRecordsInMem);

        int i = 0;
        for (SAMRecord record : samReader) {
            if (i > maxRecordsInMem && i % 1000000 == 0) {
                LOGGER.warn(maxRecordsInMem + " records in mem, this is the max, will be spilling to disk");
            }

            logProgress(i, 1000000, "SubsetBam");

            // Strip the PE specifc part from the readname so it matched the iPCR
            String name = record.getReadName().split(" ")[0];
            if (ipcrUniqueReads.contains(name)) {
                testing.add(record);
            }
            i++;
        }
        samReader.close();
        LOGGER.info("");
        LOGGER.info("Done sorting, " + i + " SAM records");

        // Write the output
        LOGGER.info("Writing BAM");
        SAMFileWriter outputWriter = new SAMFileWriterFactory()
                .makeSAMOrBAMWriter(outputHeader,
                        true,
                        new File(params.getOutputPrefix() + ".bam"));

        int recordsWritten = 0;

        for (SAMRecord record : testing) {
            outputWriter.addAlignment(record);
            recordsWritten++;
        }
        outputWriter.close();
        LOGGER.info("BAM written");

        LOGGER.info("Constructing index");
        // Bam index can only be constructed on existing bam file as it needs to have the info on the GZIP blocks
        // This is set by the .enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS)
        BAMIndexer indexer = new BAMIndexer(new File(params.getOutputPrefix() + ".bam.bai"), outputHeader);
        samReader = SamReaderFactory.makeDefault()
                .validationStringency(ValidationStringency.LENIENT)
                .enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS)
                .open(new File(params.getOutputPrefix() + ".bam"));

        for (SAMRecord record : samReader) {
            indexer.processAlignment(record);
        }
        indexer.finish();

        LOGGER.info("Written " + recordsWritten + " overlapping records");
        LOGGER.info("iPCR input: " + ipcrUniqueReads.size() + " this is PE so *2");
        LOGGER.info("BAM total: " + i);
        LOGGER.info("% written: " + Math.round(((double) recordsWritten / (double) i) * 100));
    }


    private Set<String> getIpcrReadNameSet() throws IOException {
        Set<String> readnames = new HashSet<>();

        for (String file : params.getInputIpcr()) {
            IpcrRecordProvider reader = new IpcrFileReader(new GenericFile(file), true);

            IpcrRecord curRecord = reader.getNextRecord();
            int i = 0;
            while (curRecord != null) {
                logProgress(i, 1000000, "SubsetBam");
                i++;
                readnames.add(curRecord.getQueryReadName());
                curRecord = reader.getNextRecord();
            }
            LOGGER.info("Read " + i + " records");
        }
        return readnames;
    }


}
