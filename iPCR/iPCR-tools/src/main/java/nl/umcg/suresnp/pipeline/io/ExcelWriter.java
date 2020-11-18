package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.summarystatistic.GeneticVariant;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class ExcelWriter {

    private GenericFile output;
    private ExcelStyles excelStyles;


    public ExcelWriter(GenericFile output) {
        this.output = output;
    }

    public void saveSnpAnnotationExcel(Map<String, GeneticVariant> targetVariants, Collection<GenericGenomicAnnotation> genomicAnnotations) throws IOException {

        Workbook enrichmentWorkbook = new XSSFWorkbook();
        excelStyles = new ExcelStyles(enrichmentWorkbook);
        CreationHelper createHelper = enrichmentWorkbook.getCreationHelper();

        // Put the content
        populateSnpAnntoationSheet(enrichmentWorkbook, targetVariants, genomicAnnotations);


        // write
        enrichmentWorkbook.write(output.getAsOutputStream());


    }


    private void populateSnpAnntoationSheet(Workbook workbook, Map<String, GeneticVariant> targetVariants, Collection<GenericGenomicAnnotation> genomicAnnotations ) {




        for(String variantId : targetVariants.keySet()) {
            GeneticVariant curVariant = targetVariants.get(variantId);


        }



    }
}
