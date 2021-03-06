package nl.umcg.suresnp.pipeline.io.bedreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class FourColBedFileReader implements BedRecordProvider {

    private static final Logger LOGGER = Logger.getLogger(FourColBedFileReader.class);
    private BufferedReader reader;
    private static String sep = "\t";
    private boolean trimChrFromContig;

    public FourColBedFileReader(GenericFile inputFile) throws IOException {
        this(inputFile, false);
    }
    public FourColBedFileReader(GenericFile inputFile, boolean trimChrFromContig) throws IOException {
        this.reader = inputFile.getAsBufferedReader();
        this.trimChrFromContig = trimChrFromContig;
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

    protected static BedRecord parseBedRecord(String line) {
        return parseBedRecord(line, false);
    }

    protected static BedRecord parseBedRecord(String line, boolean trimChrFromContig) {
        String[] content = line.split(sep);
        String contig = content[0];
        if (trimChrFromContig) {
            contig = contig.replaceFirst("chr", "");
        }
        if (content.length == 3) {
            return new BedRecord(contig,
                    Integer.parseInt(content[1]),
                    Integer.parseInt(content[2]),
                    0);
        }
        return new BedRecord(contig,
                Integer.parseInt(content[1]),
                Integer.parseInt(content[2]),
                Double.parseDouble(content[3]));
    }

    private BedRecord getNextRecord() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            return parseBedRecord(line, trimChrFromContig);
        } else {
            return null;
        }
    }
}
