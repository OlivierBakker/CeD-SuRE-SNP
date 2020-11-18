package nl.umcg.suresnp.pipeline.io.bedreader;

import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface GenomicAnnotationProvider extends BedRecordProvider, Iterator<GenericGenomicAnnotationRecord>, Iterable<GenericGenomicAnnotationRecord>  {
    GenericGenomicAnnotationRecord getNextGenomicAnnotation() throws IOException;
    List<GenericGenomicAnnotationRecord> getGenericGenomicAnnotationsAsList() throws IOException;
    String[] getHeader();
}
