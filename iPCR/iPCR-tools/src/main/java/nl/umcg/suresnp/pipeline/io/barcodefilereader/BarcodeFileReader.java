package nl.umcg.suresnp.pipeline.io.barcodefilereader;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BarcodeFileReader {

    Map<String, InfoRecord> readBarcodeFile(GenericFile file) throws IOException;

    Map<String, InfoRecord> readBarcodeFile(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    Map<String, String> readBarcodeFileAsStringMap(GenericFile file) throws IOException;

    Map<String, String> readBarcodeFileAsStringMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    Map<String, Integer> readBarcodeCountFile(File file) throws IOException;

    void close() throws IOException;
}
