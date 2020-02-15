package nl.umcg.suresnp.pipeline.io.bedreader;

import htsjdk.samtools.util.Locatable;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class NarrowPeakReader implements BedRecordProvider {

    private static final Logger LOGGER = Logger.getLogger(NarrowPeakReader.class);
    private BufferedReader reader;
    private String sep = "\t";

    public NarrowPeakReader(GenericFile inputFile) throws IOException {
        this.reader = inputFile.getAsBufferedReader();
    }

    @Override
    public NarrowPeakRecord getNextRecord() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            return parseNarrowPeakRecord(line);
        } else {
            return null;
        }
    }

    @Override
    public List<BedRecord> getBedRecordAsList() throws IOException {
        List<BedRecord> output = new TreeList<>();
        NarrowPeakRecord curRecord = getNextRecord();
        int i = 0;

        while (curRecord != null) {
            logProgress(i, 1000000, "NarrowPeakReader");
            i++;
            output.add(new BedRecord(curRecord));
            curRecord = getNextRecord();
        }
        LOGGER.info("Read " + i + " records");
        return output;
    }

    public List<NarrowPeakRecord> getNarrowPeakRecordsAsList() throws IOException {
        List<NarrowPeakRecord> output = new TreeList<>();
        NarrowPeakRecord curRecord = getNextRecord();
        int i = 0;

        while (curRecord != null) {
            logProgress(i, 1000000, "NarrowPeakReader");
            i++;
            output.add(curRecord);
            curRecord = getNextRecord();
        }
        LOGGER.info("Read " + i + " records");
        return output;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private NarrowPeakRecord parseNarrowPeakRecord(String line) {
        String[] content = line.split(sep);
        return new NarrowPeakRecord(content[0],
                Integer.parseInt(content[1]),
                Integer.parseInt(content[2]),
                content[3],
                Double.parseDouble(content[4]),
                content[5].charAt(0),
                Double.parseDouble(content[6]),
                Double.parseDouble(content[7]),
                Double.parseDouble(content[8]),
                Integer.parseInt(content[9]));
    }
}
