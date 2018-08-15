package nl.umcg.suresnp.pipeline.io;

import java.io.BufferedReader;
import java.io.IOException;

public class CSVReader {

    private BufferedReader reader;
    private String separator;


    public CSVReader(BufferedReader reader, String separator) {
        this.reader = reader;
        this.separator = separator;
    }

    public String[] readNext() throws IOException {
        try {
            return reader.readLine().split(separator);
        } catch (NullPointerException e) {
            return null;
        }
    }


    public void close() throws IOException {
        reader.close();
    }
}
