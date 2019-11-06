package nl.umcg.suresnp.pipeline;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import org.apache.log4j.Logger;

import java.io.IOException;

public class NormalizeCdnaWithIpcr {
    private static final Logger LOGGER = Logger.getLogger(NormalizeCdnaWithIpcr.class);

    public void run(String[] args) {

        try {
            LOGGER.info(args[2]);
            IpcrRecordProvider provider = new IpcrFileReader(new GenericFile(args[2]));

            LOGGER.info(provider.getNextRecord().getBarcode());


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
