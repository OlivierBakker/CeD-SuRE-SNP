package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.summarystatistic.*;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    private GenericFile output;
    private ExcelStyles excelStyles;

    public ExcelWriter(GenericFile output) {
        this.output = output;
    }

    public void saveSnpAnnotationExcel(Map<String, GeneticVariant> targetVariants, Map<String, List<GenericGenomicAnnotation>> genomicAnnotations, Map<String, List<VariantBasedNumericGenomicAnnotation>> variantAnnotations) throws IOException {

        System.setProperty("java.awt.headless", "true");
        Workbook enrichmentWorkbook = new XSSFWorkbook();
        excelStyles = new ExcelStyles(enrichmentWorkbook);


        // Determine the shared genomic annotations
        List<GenericGenomicAnnotation> universalGenomicAnnotations = null;
        if (genomicAnnotations.containsKey("AllSheets")) {
            universalGenomicAnnotations = genomicAnnotations.get("AllSheets");
            genomicAnnotations.remove("AllSheets");
        }

        // Determine the shared variant annotations
        List<VariantBasedNumericGenomicAnnotation> universalVariantAnnotations = null;
        if (variantAnnotations.containsKey("AllSheets")) {
            universalVariantAnnotations = variantAnnotations.get("AllSheets");
            variantAnnotations.remove("AllSheets");
        }

        // Put the content
        for (String sheet: variantAnnotations.keySet()) {

            // Define genomic annotations for this sheet
            List<GenericGenomicAnnotation> curGenomicAnnotations = genomicAnnotations.get(sheet);
            if (curGenomicAnnotations == null) {
                curGenomicAnnotations = new ArrayList<>();
            }

            if (universalGenomicAnnotations != null) {
                curGenomicAnnotations.addAll(universalGenomicAnnotations);
            }

            // Define genomic annotations for this sheet
            List<VariantBasedNumericGenomicAnnotation> curVariantAnnotations = variantAnnotations.get(sheet);
            if (curVariantAnnotations == null) {
                curVariantAnnotations = new ArrayList<>();
            }

            if (universalVariantAnnotations != null) {
                curVariantAnnotations.addAll(universalVariantAnnotations);
            }

            // Populate the sheet
            populateUncollapsedSnpAnntoationSheet(enrichmentWorkbook, sheet, targetVariants, curGenomicAnnotations, curVariantAnnotations);
        }

        // write
        enrichmentWorkbook.write(output.getAsFileOutputStream());

    }


    private void populateUncollapsedSnpAnntoationSheet(Workbook workbook,
                                                       String name,
                                                       Map<String, GeneticVariant> targetVariants,
                                                       Collection<GenericGenomicAnnotation> genomicAnnotations,
                                                       Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations) {
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

        // Init the sheet
        XSSFSheet variantOverview = initializeSheet(workbook, name, numberOfRows, numberOfCols);

        // Header
        setVariantBasedHeader(variantOverview, variantAnnotations, genomicAnnotations);

        // Populate rows
        int idNumber = 0;
        int r = 1; //+1 for header

        for (String variantId : targetVariants.keySet()) {
            GeneticVariant curVariant = targetVariants.get(variantId);
            XSSFRow row = variantOverview.createRow(r);

            // Keeps track of column
            int c = 0;

            // Set fixed variant annotations
            c = fillVariantAnnotationCells(row, c, idNumber, curVariant);

            // Set dynamic variant annotations
            c = fillDynamicVariantBasedAnnotationCells(row, c, curVariant, variantAnnotations);

            // Genomic annotations, multiple per row
            c = fillDynamicGenomicAnnotationCells(variantOverview, c, r, idNumber, curVariant, genomicAnnotations, variantAnnotations, true);

            idNumber++;
            r = variantOverview.getLastRowNum() + 1;
        }

        // Freeze the header and first row
        variantOverview.createFreezePane(1, 1);

        // Set the zoom level
        variantOverview.setZoom(75);

        // Size columns
        autoSizeColumns(variantOverview, numberOfCols-1);
    }


    private int fillVariantAnnotationCells(XSSFRow row, int c, int id, GeneticVariant curVariant) {
        // Locus ID
        XSSFCell idCell = row.createCell(c++, CellType.STRING);
        idCell.setCellValue(id);

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

        return c;
    }

    private int fillDynamicVariantBasedAnnotationCells(XSSFRow row, int c, GeneticVariant curVariant, Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations) {
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

        return c;
    }

    private int fillDynamicGenomicAnnotationCells(XSSFSheet variantOverview, int c, int r, int id, GeneticVariant curVariant, Collection<GenericGenomicAnnotation> genomicAnnotations, Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations, boolean populateVariantAnnotationsInSubRows) {
        for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
            Collection<GenericGenomicAnnotationRecord> curRecords = curAnnot.query(curVariant);
            for (int j = 0; j < curAnnot.getHeader().length; j++) {
                int subRow = 0;
                for (GenericGenomicAnnotationRecord curRecord : curRecords) {

                    XSSFRow row = variantOverview.getRow(r + subRow);
                    if (row == null) {
                        row = variantOverview.createRow(r + subRow);
                    }

                    if (curRecord.getAnnotations().size() == curAnnot.getHeader().length) {
                        // ID
                        XSSFCell idCell = row.createCell(0, CellType.STRING);
                        idCell.setCellValue(id);

                        // Dynamic annotation
                        XSSFCell curCell = row.createCell(c + j, CellType.STRING);
                        curCell.setCellValue(curRecord.getAnnotations().get(j));
                        subRow++;
                    }

                    if (populateVariantAnnotationsInSubRows) {
                        int tmp  = fillVariantAnnotationCells(row, 0, id, curVariant);
                        fillDynamicVariantBasedAnnotationCells(row, tmp, curVariant, variantAnnotations);
                    }
                }

            }
            c += curAnnot.getHeader().length;
        }

        return c;
    }

    private static void setVariantBasedHeader(XSSFSheet variantOverview, Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations, Collection<GenericGenomicAnnotation> genomicAnnotations) {

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
    }

    private static void autoSizeColumns(XSSFSheet variantOverview, int numberOfCols) {
        for (int c = 0; c < numberOfCols; c++) {
            variantOverview.autoSizeColumn(c);
            variantOverview.setColumnWidth(c, variantOverview.getColumnWidth(c) + 200); //compensate for with auto filter and inaccuracies
            if (variantOverview.getColumnWidth(c) > 5000) {
                variantOverview.setColumnWidth(c, 5000);
            }
        }
    }

    private static XSSFSheet initializeSheet(Workbook workbook, String name, int numberOfRows, int numberOfCols) {
        // Create the sheet
        XSSFSheet variantOverview = (XSSFSheet) workbook.createSheet(name);
        XSSFTable table = variantOverview.createTable(new AreaReference(new CellReference(0, 0),
                new CellReference(numberOfRows,
                        numberOfCols),
                SpreadsheetVersion.EXCEL2007));

        // Styling
        table.setName(name);
        table.setDisplayName(name);
        table.setStyleName("TableStyleLight9");
        table.getCTTable().getTableStyleInfo().setShowRowStripes(true);
        table.getCTTable().addNewAutoFilter();

        return variantOverview;
    }
}
