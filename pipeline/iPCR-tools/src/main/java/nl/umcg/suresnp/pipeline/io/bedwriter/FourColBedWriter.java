package nl.umcg.suresnp.pipeline.io.bedwriter;

import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.IpcrRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FourColBedWriter {

    protected BufferedWriter writer;
    private final String sep = "\t";


    public FourColBedWriter(File outputPrefix, boolean isZipped) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath() + FileExtensions.BED + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
    }

    public FourColBedWriter(File outputPrefix, boolean isZipped, FileExtensions extension) throws IOException {
        String suffix = ""; if (isZipped) suffix = ".gz";
        writer = new GenericFile(outputPrefix.getPath() + extension + suffix, StandardCharsets.US_ASCII).getAsBufferedWriter();
    }

    public void writeRecord(BedRecord record) throws IOException {
        writer.write(record.getContig());
        writer.write(sep);
        writer.write(Integer.toString(record.getStart()));
        writer.write(sep);
        writer.write(Integer.toString(record.getEnd()));
        writer.write(sep);
        writer.write(Double.toString(record.getScore()));
        writer.newLine();
    }

    public void writeHeader(BedRecord record) throws IOException {
        writer.write("chr");
        writer.write(sep);
        writer.write("start");
        writer.write(sep);
        writer.write("end");
        writer.write(sep);
        writer.write("score");
        writer.newLine();
    }

    public void flushAndClose() throws IOException {
        writer.flush();
        writer.close();
    }


}
