package nl.umcg.suresnp.pipeline.tools.runners;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrreader.BinaryIpcrReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.io.ipcrwriter.IpcrOutputWriter;
import nl.umcg.suresnp.pipeline.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.RecodeParameters;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class Recode {

    private static final Logger LOGGER = Logger.getLogger(Recode.class);
    private RecodeParameters params;

    public Recode(RecodeParameters params) {
        this.params = params;
    }

    public void run() throws IOException {

        GenericFile inputFile = new GenericFile(params.getInputIpcr()[0]);


        IpcrRecordProvider provider;
        switch (params.getInputType()) {
            case "IPCR":
                provider = new IpcrFileReader(inputFile, true);
                break;
            case "IPCR_BIN":
                provider = new BinaryIpcrReader(inputFile);
                break;
            default:
                throw new IllegalArgumentException("No valid input type provided");
        }


        long start = System.currentTimeMillis();
        List<IpcrRecord> input = provider.getRecordsAsList();
        long stop = System.currentTimeMillis();
        LOGGER.info("Done reading. Took: " + ((stop - start) / 1000) + " seconds");
        provider.close();

        IpcrOutputWriter writer = params.getOutputWriter();

        start = System.currentTimeMillis();
        for (IpcrRecord rec : input) {
            writer.writeRecord(rec);
        }
        stop = System.currentTimeMillis();
        LOGGER.info("Done writing. Took: " + ((stop - start) / 1000) + " seconds");
        writer.flushAndClose();

    }
}
