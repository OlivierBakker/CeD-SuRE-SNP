package nl.umcg.suresnp.pipeline.io.summarystatisticreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.summarystatistic.GeneticVariantInterval;
import nl.umcg.suresnp.pipeline.records.summarystatistic.VariantBasedNumericGenomicAnnotationRecord;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class ReferenceDependentSummaryStatisticReader implements Iterator<VariantBasedNumericGenomicAnnotationRecord>, Iterable<VariantBasedNumericGenomicAnnotationRecord> {

    private static final Logger LOGGER = Logger.getLogger(ReferenceDependentSummaryStatisticReader.class);
    private BufferedReader reader;
    private BufferedWriter missingFile;
    private static String sep = "\t";
    private String[] header;
    private Map<String, GeneticVariantInterval> referenceStatistics;

    public ReferenceDependentSummaryStatisticReader(GenericFile inputFile, GenericFile outputFile, Map<String, GeneticVariantInterval> referenceStatistics, boolean hasHeader) throws IOException {
        this.reader = inputFile.getAsBufferedReader();
        // Preserve the first buffer, header may not be longer than 8k characters
        this.reader.mark(8192);

        if (hasHeader) {
            header = reader.readLine().split(sep);
        } else {
            header = new String[reader.readLine().split(sep).length];
            for (int i = 0; i < header.length; i++) {
                header[i] = inputFile.getBaseName() + "_" + i;
            }
            // Reset the reader
            this.reader.reset();
        }

        this.referenceStatistics = referenceStatistics;
        this.missingFile = outputFile.getAsBufferedWriter();
    }

    @Override
    public Iterator<VariantBasedNumericGenomicAnnotationRecord> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        boolean result = false;
        try {
            result = reader.ready();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public VariantBasedNumericGenomicAnnotationRecord next() {
        VariantBasedNumericGenomicAnnotationRecord result = null;
        try {
            result = getNextSummaryStatisticRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void close() throws IOException {
        reader.close();
        missingFile.flush();
        missingFile.close();
    }

    public String[] getHeader() {
        return header;
    }

    private VariantBasedNumericGenomicAnnotationRecord getNextSummaryStatisticRecord() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            try {
                return parseSummaryStatisticRecord(line);
            } catch (MissingVariantException e) {
                missingFile.write(line);
                missingFile.newLine();
                return null;
            }
        } else {
            return null;
        }
    }

    private VariantBasedNumericGenomicAnnotationRecord parseSummaryStatisticRecord(String line) throws MissingVariantException {
        String[] curs = line.split(sep);
        if (referenceStatistics.containsKey(curs[0])) {
            List<Double> curAnnots = new ArrayList<>();
            for (int i = 1; i < header.length; i++) {
                curAnnots.add(Double.parseDouble(curs[i]));
            }
            return new VariantBasedNumericGenomicAnnotationRecord(referenceStatistics.get(curs[0]), curAnnots);
        } else {
            throw new MissingVariantException(curs[0] + " is not available");
        }
    }
}
