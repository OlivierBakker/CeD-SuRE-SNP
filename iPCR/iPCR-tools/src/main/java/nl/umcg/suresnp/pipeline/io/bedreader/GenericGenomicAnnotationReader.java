package nl.umcg.suresnp.pipeline.io.bedreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class GenericGenomicAnnotationReader implements GenomicAnnotationProvider {

    private static final Logger LOGGER = Logger.getLogger(GenericGenomicAnnotationReader.class);
    private BufferedReader reader;
    private static String sep = "\t";
    private String type;
    private String[] header;

    public GenericGenomicAnnotationReader(GenericFile inputFile, boolean hasHeader) throws IOException {
        this.reader = inputFile.getAsBufferedReader();
        // Preserve the first buffer, header may not be longer than 8k characters
        this.reader.mark(8192);

        switch (inputFile.getFullExtension()) {
            case ".narrowPeak":
                type="NARROW_PEAK";
                header = new String[]{"chr", "start", "end","name", "score", "strand", "signalValue", "pvalue", "qvalue"};
                break;
            default:
                type="GENERIC";
                if (hasHeader) {
                    header = reader.readLine().split(sep);
                } else {
                    header = new String[reader.readLine().split(sep).length];
                    for(int i=0; i < header.length; i++) {
                        header[i] = inputFile.getBaseName() + "_" + i;
                    }
                    // Reset the reader
                    this.reader.reset();
                }
        }

    }

    @Override
    public GenericGenomicAnnotationRecord getNextGenomicAnnotation() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            switch (type) {
                case "NARROW_PEAK":
                    return new GenericGenomicAnnotationRecord(NarrowPeakReader.parseNarrowPeakRecord(line));
                case "BED":
                    return new GenericGenomicAnnotationRecord(FourColBedFileReader.parseBedRecord(line));
                default:
                    return parseGenomicAnnotation(line);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<GenericGenomicAnnotationRecord> getGenericGenomicAnnotationsAsList() throws IOException {
        List<GenericGenomicAnnotationRecord> output = new TreeList<>();
        GenericGenomicAnnotationRecord curRecord = getNextGenomicAnnotation();
        int i = 0;

        while (curRecord != null) {
            logProgress(i, 10000, "GenericGenomicAnnotationReader");
            i++;
            output.add(curRecord);
            curRecord = getNextGenomicAnnotation();
        }
        LOGGER.info("Read " + i + " records");
        return output;
    }

    @Override
    public String[] getHeader() {
        return header;
    }

    @Override
    public BedRecord getNextRecord() throws IOException {
        return this.getNextGenomicAnnotation();
    }

    @Override
    public List<BedRecord> getBedRecordAsList() throws IOException {
        final List<BedRecord> genericGenomicAnnotationsAsList = (List<BedRecord>) (List<?>) this.getGenericGenomicAnnotationsAsList();
        return genericGenomicAnnotationsAsList;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    protected GenericGenomicAnnotationRecord parseGenomicAnnotation(String line) {
        String[] curLine = line.split(sep);
        GenericGenomicAnnotationRecord curAnnot = new GenericGenomicAnnotationRecord(
                curLine[0],
                Integer.parseInt(curLine[1]),
                Integer.parseInt(curLine[2]));

        for (int i = 3; i < curLine.length; i++) {
            curAnnot.addAnnotation(curLine[i]);
        }

        return curAnnot;
    }

    @Override
    public Iterator<GenericGenomicAnnotationRecord> iterator() {
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
    public GenericGenomicAnnotationRecord next() {

        GenericGenomicAnnotationRecord result=null;
        try {
            result = getNextGenomicAnnotation();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
