package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.summarystatistic.*;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    private GenericFile output;
    private ExcelStyles excelStyles;

    public ExcelWriter(GenericFile output) {
        this.output = output;
    }

    public void saveSnpAnnotationExcel(Map<String, GeneticVariant> targetVariants, Collection<GenericGenomicAnnotation> genomicAnnotations, Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations) throws IOException {

        System.setProperty("java.awt.headless", "true");
        Workbook enrichmentWorkbook = new XSSFWorkbook();
        excelStyles = new ExcelStyles(enrichmentWorkbook);

        // Put the content
        populateSnpAnntoationSheet(enrichmentWorkbook, targetVariants, genomicAnnotations, variantAnnotations);

        // write
        enrichmentWorkbook.write(output.getAsFileOutputStream());

    }


    private void populateSnpAnntoationSheet(Workbook workbook, Map<String, GeneticVariant> targetVariants, Collection<GenericGenomicAnnotation> genomicAnnotations, Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations) {
        int numberOfCols = 6;
        int numberOfRows = targetVariants.size();

        // Determine number of columns
        for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
            numberOfCols += curAnnot.getHeader().length;
        }

        // Determine number of rows
        for (VariantBasedNumericGenomicAnnotation curAnnot : variantAnnotations) {
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
        headerRow.createCell(hc++, CellType.STRING).setCellValue("variant_id");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("chromosome");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("position");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("allele_1");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("allele_2");

        for (VariantBasedNumericGenomicAnnotation curAnnot : variantAnnotations) {
            for (String curHeader : curAnnot.getHeader()) {
                headerRow.createCell(hc++, CellType.STRING).setCellValue(curHeader);
            }
        }

        for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
            for (String curHeader : curAnnot.getHeader()) {
                headerRow.createCell(hc++, CellType.STRING).setCellValue(curHeader);
            }
        }

        // Populate rows
        int i = 0;
        int r = 1; //+1 for header
        boolean flipFlop = false;

        for (String variantId : targetVariants.keySet()) {
            GeneticVariant curVariant = targetVariants.get(variantId);

            XSSFRow row = variantOverview.createRow(r);

            int c = 0;

            // Locus ID
            XSSFCell idCell = row.createCell(c++, CellType.STRING);
            idCell.setCellValue(i);

            // Variant id
            XSSFCell locusNameCell = row.createCell(c++, CellType.STRING);
            locusNameCell.setCellValue(curVariant.getPrimaryVariantId());
            locusNameCell.setCellStyle(excelStyles.getBoldStyle());

            // Chromosome
            XSSFCell chrCell = row.createCell(c++, CellType.NUMERIC);
            chrCell.setCellValue(curVariant.getContig());
            chrCell.setCellStyle(excelStyles.getBoldStyle());

            // Position
            XSSFCell startCell = row.createCell(c++, CellType.NUMERIC);
            startCell.setCellValue(curVariant.getPosition());
            startCell.setCellStyle(excelStyles.getBoldGenomicPositionStyle());

            // Allele 1
            XSSFCell a1Cell = row.createCell(c++, CellType.NUMERIC);
            a1Cell.setCellValue(curVariant.getAllele1());
            a1Cell.setCellStyle(excelStyles.getBoldStyle());

            // Allele 2
            XSSFCell a2Cell = row.createCell(c++, CellType.NUMERIC);
            a2Cell.setCellValue(curVariant.getAllele2());
            a2Cell.setCellStyle(excelStyles.getBoldStyle());

            // Variant annotations, only one per row
            for (VariantBasedNumericGenomicAnnotation curAnnot : variantAnnotations) {
                VariantBasedNumericGenomicAnnotationRecord curRec = curAnnot.query(curVariant.getPrimaryVariantId());

                if (curRec != null) {
                    for (int j = 0; j < curAnnot.getHeader().length; j++) {
                        XSSFCell betaCell = row.createCell(c++, CellType.NUMERIC);
                        betaCell.setCellStyle(excelStyles.getZscoreStyle());
                        betaCell.setCellValue(curRec.getAnnotations().get(j));
                    }
                } else {
                    for (int j = 0; j < curAnnot.getHeader().length; j++) {
                        row.createCell(c++, CellType.BLANK);
                    }
                }

            }

            // Genomic annotations, multiple per row
            for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
                Collection<GenericGenomicAnnotationRecord> curRecords = curAnnot.query(curVariant);
                for (int j = 0; j < curAnnot.getHeader().length; j++) {
                    int subRow = 0;
                    for (GenericGenomicAnnotationRecord curRecord : curRecords) {

                        row = variantOverview.getRow(r + subRow);
                        if (row == null) {
                            row = variantOverview.createRow(r + subRow);
                        }

                        if (curRecord.getAnnotations().size() == curAnnot.getHeader().length) {
                            // ID
                            idCell = row.createCell(0, CellType.STRING);
                            idCell.setCellValue(i);

                            // Dynamic annotation
                            XSSFCell curCell = row.createCell(c + j, CellType.STRING);
                            curCell.setCellValue(curRecord.getAnnotations().get(j));
                            subRow++;
                        }

                    }

                }
                c += curAnnot.getHeader().length;
            }


            i++;
            r = variantOverview.getLastRowNum() + 1;
            flipFlop = (i % 2) > 0;
        }


        variantOverview.createFreezePane(1,1);
        variantOverview.setZoom(75);
        for (int c = 0; c < numberOfCols; c++) {
            variantOverview.autoSizeColumn(c);
            variantOverview.setColumnWidth(c, variantOverview.getColumnWidth(c) + 200); //compensate for with auto filter and inaccuracies
            if (variantOverview.getColumnWidth(c) > 5000) {
                variantOverview.setColumnWidth(c, 5000);
            }
        }
    }

}
