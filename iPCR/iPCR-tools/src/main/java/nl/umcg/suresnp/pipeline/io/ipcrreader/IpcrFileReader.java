package nl.umcg.suresnp.pipeline.io.ipcrreader;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.ipcrrecords.BasicIpcrRecord;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class IpcrFileReader implements IpcrRecordProvider {

    private static final Logger LOGGER = Logger.getLogger(IpcrFileReader.class);
    private BufferedReader reader;
    private String sep;
    private String[] header;
    private String[] cdnaSamples;

    public IpcrFileReader(GenericFile file) throws IOException {
        reader = new BufferedReader(new InputStreamReader(file.getAsInputStream()));
        sep="\t";
        setHeader();
    }

    public IpcrFileReader(GenericFile file, String sep) throws IOException {
        reader = new BufferedReader(new InputStreamReader(file.getAsInputStream()));
        this.sep = sep;
        setHeader();
    }

    private void setHeader() throws IOException {
        String line = reader.readLine();
        header = line.split(sep);
        if (header.length < 15) {
            LOGGER.error("Error parsing line:");
            LOGGER.error(line);
            LOGGER.error("Needed 15 columns in IPCR file, found: " + header.length);
            throw new IllegalArgumentException("Needed 15 columns in IPCR file, found: " + header.length);
        }

        if (header.length > 15) {
            LOGGER.info("Detected barcode counts for samples:");
            cdnaSamples = new String[header.length-15];

            int i=15;
            int j=0;
            while (i < header.length) {
                LOGGER.info(header[i]);
                cdnaSamples[j] = header[i];
                i++;
                j++;
            }

        }
    }

    @Override
    public IpcrRecord getNextRecord() throws IOException {
        String line = reader.readLine();

        if (line != null) {
            return parseIpcrRecord(line);
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private IpcrRecord parseIpcrRecord(String line) {
        IpcrRecord record = new BasicIpcrRecord();
        String[] data = line.split(sep);

        if (data.length < 15) {
            LOGGER.error("Error parsing line:");
            LOGGER.error(line);
            LOGGER.error("Needed 15 columns in IPCR file, found: " + data.length);
            throw new IllegalArgumentException("Needed 15 columns in IPCR file, found: " + data.length);
        }
        record.setBarcode(data[0]);
        record.setPrimaryReadName(data[1]);
        //record.setMateReadName(data[1]);
        record.setContig(data[2]);
        record.setPrimaryStart(Integer.parseInt(data[3]));
        record.setPrimaryEnd(Integer.parseInt(data[4]));
        record.setMateStart(Integer.parseInt(data[5]));
        record.setMateEnd(Integer.parseInt(data[6]));
        record.setPrimarySamFlags(Integer.parseInt(data[7]));
        record.setMateSamFlags(Integer.parseInt(data[8]));
        record.setPrimaryMappingQuality(Integer.parseInt(data[9]));
        record.setMateMappingQuality(Integer.parseInt(data[10]));
        record.setPrimaryCigar(data[11]);
        record.setMateCigar(data[12]);
        record.setPrimaryStrand(data[13].charAt(0));
        record.setMateStrand(data[14].charAt(0));

        if (cdnaSamples != null) {
            Map<String, Integer> curBarcodeCounts = new HashMap<>();
            int i=0;
            for (String sample: cdnaSamples) {
                curBarcodeCounts.put(sample, Integer.parseInt(data[15 + i]));
                i++;
            }
            record.setBarcodeCountPerSample(curBarcodeCounts);
        }
        return record;
    }
}
