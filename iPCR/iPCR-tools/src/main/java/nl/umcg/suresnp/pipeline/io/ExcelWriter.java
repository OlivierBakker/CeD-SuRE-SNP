package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.summarystatistic.GeneticVariant;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    private GenericFile output;
    private ExcelStyles excelStyles;

    public ExcelWriter(GenericFile output) {
        this.output = output;
    }

    public void saveSnpAnnotationExcel(Map<String, GeneticVariant> targetVariants, Collection<GenericGenomicAnnotation> genomicAnnotations) throws IOException {

        System.setProperty("java.awt.headless", "true");
        Workbook enrichmentWorkbook = new XSSFWorkbook();
        excelStyles = new ExcelStyles(enrichmentWorkbook);

        // Put the content
        populateSnpAnntoationSheet(enrichmentWorkbook, targetVariants, genomicAnnotations);

        // write
        enrichmentWorkbook.write(output.getAsFileOutputStream());

    }


    private void populateSnpAnntoationSheet(Workbook workbook, Map<String, GeneticVariant> targetVariants, Collection<GenericGenomicAnnotation> genomicAnnotations) {
        int numberOfCols = 6;
        int numberOfRows = targetVariants.size();
        for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
            numberOfCols += curAnnot.getHeader().length;
        }

        // Create the sheet
        XSSFSheet variantOverview = (XSSFSheet) workbook.createSheet("VariantOverview");
        XSSFTable table = variantOverview.createTable(new AreaReference(new CellReference(0, 0),
                new CellReference(numberOfRows,
                        numberOfCols),
                SpreadsheetVersion.EXCEL2007));

        // Styling
        table.setName("VariantOverview");
        table.setDisplayName("VariantOverview");
        table.setStyleName("TableStyleLight9");
        table.getCTTable().getTableStyleInfo().setShowRowStripes(true);
        table.getCTTable().addNewAutoFilter();

        // Header
        XSSFRow headerRow = variantOverview.createRow(0);

        int hc = 0;
        headerRow.createCell(hc++, CellType.STRING).setCellValue("id");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("Variant id");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("Chromosome");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("Position");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("Allele1");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("Allele2");
        for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
            for (String curHeader : curAnnot.getHeader()) {
                headerRow.createCell(hc++, CellType.STRING).setCellValue(curHeader);
            }
        }

        // Populate rows
        int i = 0;
        int r = 1; //+1 for header
        for (String variantId : targetVariants.keySet()) {
            GeneticVariant curVariant = targetVariants.get(variantId);

            XSSFRow row = variantOverview.createRow(r);

            // Locus ID
            XSSFCell idCell = row.createCell(0, CellType.STRING);
            idCell.setCellValue(i);

            // Variant id
            XSSFCell locusNameCell = row.createCell(1, CellType.STRING);
            locusNameCell.setCellValue(curVariant.getPrimaryVariantId());

            // Chromosome
            XSSFCell chrCell = row.createCell(2, CellType.NUMERIC);
            chrCell.setCellValue(curVariant.getContig());

            // Position
            XSSFCell startCell = row.createCell(3, CellType.NUMERIC);
            startCell.setCellValue(curVariant.getPosition());
            startCell.setCellStyle(excelStyles.getGenomicPositionStyle());

            // Allele 1
            XSSFCell a1Cell = row.createCell(4, CellType.NUMERIC);
            a1Cell.setCellValue(curVariant.getAllele1());

            // Allele 2
            XSSFCell a2Cell = row.createCell(5, CellType.NUMERIC);
            a2Cell.setCellValue(curVariant.getAllele2());

            int c = 6;

            for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
                Collection<GenericGenomicAnnotationRecord> curRecords = curAnnot.query(curVariant);
                for (int j = 0; j < curAnnot.getHeader().length; j++) {
                    int subRow = 0;
                    for (GenericGenomicAnnotationRecord curRecord: curRecords) {

                        row = variantOverview.getRow(r + subRow);

                        if (row == null) {
                            row = variantOverview.createRow(r + subRow);
                        }

                        // ID
                        idCell = row.createCell(0, CellType.STRING);
                        idCell.setCellValue(i);

                        // Dynamic annotation
                        XSSFCell curCell = row.createCell(c + j, CellType.STRING);
                        curCell.setCellValue(curRecord.getAnnotations().get(j));
                        subRow++;
                    }

                }
                c += curAnnot.getHeader().length;
            }

            r = variantOverview.getLastRowNum() +1;
            i++;
        }

        for (int c = 0; c < numberOfCols; c++) {
            variantOverview.autoSizeColumn(c);
            variantOverview.setColumnWidth(c, variantOverview.getColumnWidth(c) + 200); //compensate for with auto filter and inaccuracies
            if (c > 1 && variantOverview.getColumnWidth(c) > 20000) {
                //max col width. Not for first column.
                variantOverview.setColumnWidth(c, 20000);
            }
        }
    }

}
