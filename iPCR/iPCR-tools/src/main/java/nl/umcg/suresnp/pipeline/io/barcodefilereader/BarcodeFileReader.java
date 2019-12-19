package nl.umcg.suresnp.pipeline.io.barcodefilereader;

import nl.umcg.suresnp.pipeline.barcodes.InfoRecord;
import nl.umcg.suresnp.pipeline.barcodes.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BarcodeFileReader {

    List<InfoRecord> readBarcodeFileAsList(GenericFile file) throws IOException;

    List<InfoRecord> readBarcodeFileAsList(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file) throws IOException;

    Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    Map<String, String> readBarcodeFileAsStringMap(GenericFile file) throws IOException;

    Map<String, String> readBarcodeFileAsStringMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    Map<String, Integer> readBarcodeCountFile(GenericFile file) throws IOException;

    void flushAndClose() throws IOException;
}
