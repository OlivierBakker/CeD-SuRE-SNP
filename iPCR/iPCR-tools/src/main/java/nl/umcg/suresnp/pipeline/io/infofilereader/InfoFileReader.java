package nl.umcg.suresnp.pipeline.io.infofilereader;

import nl.umcg.suresnp.pipeline.records.inforecord.InfoRecord;
import nl.umcg.suresnp.pipeline.records.inforecord.filters.InfoRecordFilter;
import nl.umcg.suresnp.pipeline.io.GenericFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InfoFileReader {

    List<InfoRecord> readBarcodeFileAsList(GenericFile file) throws IOException;

    List<InfoRecord> readBarcodeFileAsList(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file) throws IOException;

    Map<String, InfoRecord> readBarcodeFileAsMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    Map<String, String> readBarcodeFileAsStringMap(GenericFile file) throws IOException;

    Map<String, String> readBarcodeFileAsStringMap(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    Set<String> getBarcodeSet(GenericFile file) throws IOException;

    Set<String> getBarcodeSet(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    List<String> getBarcodeList(GenericFile file) throws IOException;

    List<String> getBarcodeList(GenericFile file, List<InfoRecordFilter> filters) throws IOException;

    void flushAndClose() throws IOException;
}
