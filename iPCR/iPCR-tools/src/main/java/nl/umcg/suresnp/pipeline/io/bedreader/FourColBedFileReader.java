package nl.umcg.suresnp.pipeline.io.bedreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class FourColBedFileReader implements BedRecordProvider {

    private static final Logger LOGGER = Logger.getLogger(FourColBedFileReader.class);
    private BufferedReader reader;
    private String sep = "\t";

    public FourColBedFileReader(GenericFile inputFile) throws IOException {
        this.reader = inputFile.getAsBufferedReader();
    }

    public BedRecord getNextRecord() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            return parseFourColBedRecord(line);
        } else {
            return null;
        }
    }

    public List<BedRecord> getBedRecordAsList() throws IOException {
        List<BedRecord> output = new TreeList<>();
        BedRecord curRecord = getNextRecord();
        int i = 0;

        while (curRecord != null) {
            logProgress(i, 10000, "FourColBedFileReader");
            i++;
            output.add(curRecord);
            curRecord = getNextRecord();
        }
        LOGGER.info("Read " + i + " records");
        return output;
    }

    public void close() throws IOException {
        reader.close();
    }

    private BedRecord parseFourColBedRecord(String line) {
        String[] content = line.split(sep);
        return new BedRecord(content[0],
                Integer.parseInt(content[1]),
                Integer.parseInt(content[2]),
                Double.parseDouble(content[3]));
    }
}
