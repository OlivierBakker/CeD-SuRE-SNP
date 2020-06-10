package nl.umcg.suresnp.pipeline.io.ipcrreader;

import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.readers.LineIterator;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.BasicIpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to translate a BGZipped IpcrFile into an IpcrObject. Can be fed into FeatureReader\<IpcrRecord\>
 */
public class IpcrCodec extends AsciiFeatureCodec<IpcrRecord> {

    private static final Logger LOGGER = Logger.getLogger(IpcrCodec.class);
    private String[] cdnaSamples;
    private String sep;

    /**
     * Instantiates a new Ipcr codec.
     */
    public IpcrCodec() {
        super(IpcrRecord.class);
        this.sep = "\t";
    }

    /**
     * Instantiates a new Ipcr codec.
     *
     * @param cdnaSamples the cdna samples
     */
    public IpcrCodec(String[] cdnaSamples) {
        super(IpcrRecord.class);
        this.cdnaSamples = cdnaSamples;
    }

    /**
     * Sets cdna samples.
     *
     * @param samples the samples
     */
    public void setCdnaSamples(String[] samples) {
        cdnaSamples = samples;
        this.sep = "\t";
    }

    @Override
    public IpcrRecord decode(String s) {

        if (s.trim().isEmpty()) {
            return null;
        }
        // discard header lines in case the caller hasn't called readHeader
        if (isHeaderLine(s)) {
            return null;
        }

        return parseFullIpcrRecord(s, cdnaSamples, sep);
    }

    @Override
    public Object readActualHeader(LineIterator lineIterator) {
        // see BEDCodec
        while (lineIterator.hasNext()) {
            // Only peek, since we don't want to actually consume a line of input unless its a header line.
            // This prevents us from advancing past the first feature.
            final String nextLine = lineIterator.peek();
            if (isHeaderLine(nextLine)) {
                // advance the iterator and consume the line (which is a no-op)
                this.parseHeaderLine(lineIterator.next());
            } else {
                return null; // break out when we've seen the end of the header
            }
        }
        return null;
    }

    private void parseHeaderLine(String line ){
        String[] header = line.split(sep);

        if (header.length < 16) {
            LOGGER.error("Error parsing line:");
            LOGGER.error(line);
            LOGGER.error("Needed 16 columns in IPCR file, found: " + header.length);
            throw new IllegalArgumentException("Needed 16 columns in IPCR file, found: " + header.length);
        }

        if (header.length > 16) {

            StringBuilder sampleLog = new StringBuilder();
            LOGGER.info("Detected barcode counts for samples:");
            cdnaSamples = new String[header.length - 16];

            int i = 16;
            int j = 0;
            while (i < header.length) {
                sampleLog.append(header[i]);
                sampleLog.append(", ");
                cdnaSamples[j] = header[i];
                i++;
                j++;
            }
            LOGGER.info(sampleLog.toString());

        }
    }

    private boolean isHeaderLine(String line) {
        return line.startsWith("barcode");
    }

    @Override
    public boolean canDecode(String path) {
        final String toDecode;
        if (IOUtil.hasBlockCompressedExtension(path)) {
            toDecode = path.substring(0, path.lastIndexOf("."));
        } else {
            toDecode = path;
        }
        return toDecode.toLowerCase().endsWith(".ipcr");
    }

    /**
     * Parse full ipcr record ipcr record.
     *
     * @param line        the line
     * @param cdnaSamples the cdna samples
     * @param sep         the sep
     * @return the ipcr record
     */
    public static IpcrRecord parseFullIpcrRecord(String line, String[] cdnaSamples, String sep) {
        String[] data = line.split(sep);
        IpcrRecord record = new BasicIpcrRecord();

        if (data.length < 16) {
            LOGGER.error("Error parsing line:");
            LOGGER.error(line);
            LOGGER.error("Needed 16 columns in IPCR file, found: " + data.length);
            throw new IllegalArgumentException("Needed 16 columns in IPCR file, found: " + data.length);
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
        record.setIpcrDuplicateCount(Integer.parseInt(data[15]));

        if (cdnaSamples != null) {
            Map<String, Integer> curBarcodeCounts = new HashMap<>();
            int i = 0;
            for (String sample : cdnaSamples) {
                curBarcodeCounts.put(sample, Integer.parseInt(data[16 + i]));
                i++;
            }
            record.setBarcodeCountPerSample(curBarcodeCounts);
        }
        return record;
    }
}
