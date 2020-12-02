package nl.umcg.suresnp.pipeline.io.bedreader;

import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.readers.LineIterator;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrCodec;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;
import org.apache.log4j.Logger;

public class GenericGenomicAnnotationCodec extends AsciiFeatureCodec<GenericGenomicAnnotationRecord> {

    private static final Logger LOGGER = Logger.getLogger(GenericGenomicAnnotationCodec.class);

    private boolean hasHeader;

    /**
     * Instantiates a new Ipcr codec.
     */
    public GenericGenomicAnnotationCodec(boolean hasHeader) {
        super(GenericGenomicAnnotationRecord.class);
        this.hasHeader = hasHeader;
    }

    @Override
    public GenericGenomicAnnotationRecord decode(String s) {
        if (s.trim().isEmpty()) {
            return null;
        }
        return GenericGenomicAnnotationReader.parseGenomicAnnotation(s);
    }

    @Override
    public Object readActualHeader(LineIterator reader) {
        if (hasHeader) {
            reader.next();
        }
        return null;
    }

    @Override
    public boolean canDecode(String path) {
        // TODO: fix this
        return true;
    }


}
