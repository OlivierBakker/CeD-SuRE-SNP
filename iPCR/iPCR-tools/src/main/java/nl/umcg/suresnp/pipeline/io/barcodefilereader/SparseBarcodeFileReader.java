package nl.umcg.suresnp.pipeline.io.barcodefilereader;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.CsvReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SparseBarcodeFileReader extends GenericBarcodeFileReader {

    public SparseBarcodeFileReader(String outputPrefix) throws IOException {
        super(outputPrefix);
    }

    @Override
    public InfoRecord parseBarcodeRecord(String[] line) throws NumberFormatException {
        InfoRecord curRec = new InfoRecord(line[0].split(" ")[0],
                Integer.parseInt(line[1]),
                Integer.parseInt(line[2]),
                Integer.parseInt(line[3]),
                line[4], null, null, null, null, null);
        return curRec;
    }

}
