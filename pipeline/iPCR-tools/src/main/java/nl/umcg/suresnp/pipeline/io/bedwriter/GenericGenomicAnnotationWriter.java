package nl.umcg.suresnp.pipeline.io.bedwriter;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;

import java.io.BufferedWriter;
import java.io.IOException;

public class GenericGenomicAnnotationWriter {

    protected BufferedWriter writer;
    private final String sep = "\t";

    public GenericGenomicAnnotationWriter(GenericFile file) throws IOException {
        this.writer = file.getAsBufferedWriter();
    }

    public void writeRecord(GenericGenomicAnnotationRecord record) throws IOException {
        writer.write(record.getContig());
        writer.write(sep);
        writer.write(Integer.toString(record.getStart()));
        writer.write(sep);
        writer.write(Integer.toString(record.getEnd()));

        for (String annotation: record.getAnnotations()) {
            writer.write(sep);
            writer.write(annotation);
        }

        writer.newLine();
    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
    }
}
