package nl.umcg.suresnp.pipeline.io.ipcrreader;

import htsjdk.samtools.util.BlockCompressedInputStream;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;


//TODO: fix duplicate code, first thingy with replacing the inputstream didnt really work
public class BlockCompressedIpcrFileReader extends IpcrFileReader implements IpcrRecordProvider  {

    private static final Logger LOGGER = Logger.getLogger(BlockCompressedIpcrFileReader.class);

    private BlockCompressedInputStream coreInputStream;
    private BufferedReader barcodeReader;
    private String sep;

    public BlockCompressedIpcrFileReader(GenericFile file, boolean hasHeader) throws IOException {

        //super(file.getPathAsString(), file.getPathAsString() + ".tbi", codec);
        setBarcodeReader(file);
        this.coreInputStream = new BlockCompressedInputStream(new File(file.getPathAsString()));
        this.sep = "\t";
        if (hasHeader) {
            setHeader();
        }
    }

    public BlockCompressedIpcrFileReader(GenericFile file, IpcrCodec codec,  boolean hasHeader) throws IOException {
        //super(file.getPathAsString(), file.getPathAsString() + ".tbi", codec);
        setBarcodeReader(file);
        this.coreInputStream = new BlockCompressedInputStream(new File(file.getPathAsString()));
        this.sep = "\t";
        if (hasHeader) {
            setHeader();
        }
    }

    @Override
    protected String getNextLine() throws IOException {
        return coreInputStream.readLine();
    }

    @Override
    public IpcrRecord getNextRecord() throws IOException {
        String line = coreInputStream.readLine();
        if (line != null) {
            return parseIpcrRecord(line);
        } else {
            return null;
        }
    }

    @Override
    public Set<String> getBarcodeSet() throws IOException {
        Set<String> barcodeSet = new HashSet<>();
        int i = 0;

        if (barcodeReader != null) {
            String line = barcodeReader.readLine();
            while (line != null) {
                logProgress(i, 1000000, "IpcrFileReader");
                i++;
                barcodeSet.add(line);
                line = barcodeReader.readLine();
            }
        } else {
            String line = coreInputStream.readLine();
            while (line != null) {
                logProgress(i, 1000000, "IpcrFileReader");
                i++;

                String[] data = line.split(sep);
                barcodeSet.add(data[0]);
                line = coreInputStream.readLine();
            }
        }

        LOGGER.info("Read " + i + " records");
        return barcodeSet;
    }

    @Override
    public void close() throws IOException {
        coreInputStream.close();
    }

    public long getFilePointer() {
        return coreInputStream.getFilePointer();
    }

    private void setBarcodeReader(GenericFile file) throws IOException {
        try {
            String suffix = "";
            if (file.isGzipped()) {
                suffix = ".gz";
            }
            String curPath = file.getFolder() + file.getFileName().trim().replaceFirst("\\.gz$", "") + ".barcodes" + suffix;
            LOGGER.info("Reading barcodes from: " + curPath);
            this.barcodeReader = new GenericFile(curPath).getAsBufferedReader();
        } catch (FileNotFoundException e) {
            LOGGER.warn("No barcode file found, certain functionality will be slower");
            this.barcodeReader = null;
        }
    }

    private IpcrRecord parseIpcrRecord(String line) {
        return IpcrCodec.parseFullIpcrRecord(line, getCdnaSamples(), sep);
    }

}
