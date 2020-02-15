package nl.umcg.suresnp.pipeline.io.ipcrreader;

import htsjdk.samtools.util.BlockCompressedInputStream;
import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.io.IOException;


public class BgzipIpcrFileReader extends IpcrFileReader {

    private BlockCompressedInputStream inputStream;

    public BgzipIpcrFileReader(GenericFile file, boolean hasHeader) throws IOException {
        super(hasHeader);
        inputStream = new BlockCompressedInputStream(file.getAsInputStream());
        setCoreInputStream(inputStream);
    }

    public long getFilePointer() {
        return inputStream.getFilePointer();
    }

}
