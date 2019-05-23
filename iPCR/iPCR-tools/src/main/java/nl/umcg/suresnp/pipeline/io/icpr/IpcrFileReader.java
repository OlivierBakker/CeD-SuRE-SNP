package nl.umcg.suresnp.pipeline.io.icpr;

import nl.umcg.suresnp.pipeline.ipcrrecords.AnnotatedIpcrRecordWithMate;
import nl.umcg.suresnp.pipeline.io.CsvReader;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class IpcrFileReader {

    private static final Logger LOGGER = Logger.getLogger(IpcrFileReader.class);

    public static List<AnnotatedIpcrRecordWithMate> readIPCRFileCollapsed(String path) throws IOException, IpcrParseException {

        LOGGER.warn("Current implementation assumes file is sorted on barcode");

        // May seem excessive, but allows for easy change to zipped files if needed
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)))), " ");

        List<AnnotatedIpcrRecordWithMate> ipcrRecords = new ArrayList<>();
        String[] line;
        int curRecord = 0;
        int prevRecord = 0;

        while ((line = reader.readNext(true)) != null) {
            // Logging
            if (curRecord > 0){if(curRecord % 1000000 == 0){LOGGER.info("Read " + curRecord / 1000000 + " million records");}}

            if (line.length != 14) {
                LOGGER.warn("Skipping line" + curRecord + " since it is of invalid length, has the separator been properly set?");
                continue;
            } else {

                AnnotatedIpcrRecordWithMate record = parseAnnotatedIPCRRecord(line, curRecord);

                // Collapse the records on barcode, keeping only the one with the highest duplicate count
                if (curRecord > 0) {
                    if (ipcrRecords.get(prevRecord).getBarcode().equals(record.getBarcode())) {
                        if (ipcrRecords.get(prevRecord).getDuplicateCount() < record.getDuplicateCount()) {
                            ipcrRecords.set(prevRecord, record);
                        }
                    } else {
                        ipcrRecords.add(record);
                        prevRecord++;
                    }

                } else {
                    ipcrRecords.add(record);
                }
            }

            curRecord++;

        }
        reader.close();

        return ipcrRecords;
    }

    private static AnnotatedIpcrRecordWithMate parseAnnotatedIPCRRecord(String [] line, int lineNumber) throws IpcrParseException {
        // To save time no support for a header, since we can safely define the format ourselves
        // Although it could be easily added if needed
        // Column order:
        // barcode, chromosome, s1, e1, s2, e2, orientation, mapq1, mapq2, cigar1, cigar2, seq1, seq2, count
        int[] ipcrColumnOrder = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0};

        // Parse the iPCR record
        AnnotatedIpcrRecordWithMate record;
        try {
            record = new AnnotatedIpcrRecordWithMate(line[ipcrColumnOrder[0]],
                    line[ipcrColumnOrder[1]],
                    Integer.parseInt(line[ipcrColumnOrder[2]]),
                    Integer.parseInt(line[ipcrColumnOrder[3]]),
                    Integer.parseInt(line[ipcrColumnOrder[4]]),
                    Integer.parseInt(line[ipcrColumnOrder[5]]),
                    line[ipcrColumnOrder[6]].charAt(0),
                    Integer.parseInt(line[ipcrColumnOrder[7]]),
                    Integer.parseInt(line[ipcrColumnOrder[8]]),
                    line[ipcrColumnOrder[9]],
                    line[ipcrColumnOrder[10]],
                    line[ipcrColumnOrder[11]],
                    line[ipcrColumnOrder[12]],
                    Integer.parseInt(line[ipcrColumnOrder[13]]));
        } catch (NumberFormatException e) {
            throw new IpcrParseException(e.getMessage(), lineNumber);
        }

        return record;
    }

    public static int getPerc(int a, int b) {
        float p = ((float)a / (float)b) *100;
        return Math.round(p);
    }

}
