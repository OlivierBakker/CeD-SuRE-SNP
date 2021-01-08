package nl.umcg.suresnp.pipeline.io.bedwriter;

import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;

public class NarrowPeakWriter {


    private BufferedWriter writer;
    private static final String sep="\t";

    public NarrowPeakWriter(GenericFile file) throws IOException {
        writer = file.getAsBufferedWriter();
    }

    public void writeRecords(Collection<NarrowPeakRecord> records) throws IOException {
        for (NarrowPeakRecord curRecord: records) {
            writeRecord(curRecord);
        }
    }

    public void writeRecord(NarrowPeakRecord record) throws IOException {
        writer.write(record.getContig());
        writer.write(sep);
        writer.write(Integer.toString(record.getStart()));
        writer.write(sep);
        writer.write(Integer.toString(record.getEnd()));
        writer.write(sep);
        writer.write(record.getName());
        writer.write(sep);
        writer.write(Double.toString(record.getScore()));
        writer.write(sep);
        writer.write(record.getStrandAsChar());
        writer.write(sep);
        writer.write(Double.toString(record.getSignalValue()));
        writer.write(sep);
        writer.write(Double.toString(record.getpValue()));
        writer.write(sep);
        writer.write(Double.toString(record.getqValue()));
        writer.write(sep);
        writer.write(Integer.toString(record.getPeak()));
        writer.newLine();
    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
    }

}
