package nl.umcg.suresnp.pipeline.io.infofilereader;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;

import java.io.IOException;


public class SparseInfoFileReader extends GenericInfoFileReader {

    public SparseInfoFileReader(String outputPrefix) throws IOException {
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
