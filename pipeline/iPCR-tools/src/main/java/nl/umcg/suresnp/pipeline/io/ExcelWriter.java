package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.ensemblrecord.AnnotatedGene;
import nl.umcg.suresnp.pipeline.records.ensemblrecord.GeneBasedGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.ensemblrecord.OverlapType;
import nl.umcg.suresnp.pipeline.records.summarystatistic.*;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class ExcelWriter {

    private GenericFile output;
    private ExcelStyles excelStyles;

    public ExcelWriter(GenericFile output) {
        this.output = output;
    }

    public void saveSnpAnnotationExcel(Map<String, GeneticVariantInterval> targetVariants,
                                       Map<String, List<GenericGenomicAnnotation>> genomicAnnotations,
                                       Map<String, List<VariantBasedGenomicAnnotation>> variantAnnotations,
                                       GeneBasedGenomicAnnotation geneAnnotations) throws IOException {

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
        List<VariantBasedGenomicAnnotation> universalVariantAnnotations = null;
        if (variantAnnotations.containsKey("AllSheets")) {
            universalVariantAnnotations = variantAnnotations.get("AllSheets");
            variantAnnotations.remove("AllSheets");
        }

        // Put the content
        for (String sheet : variantAnnotations.keySet()) {

            // Define genomic annotations for this sheet
            List<GenericGenomicAnnotation> curGenomicAnnotations = genomicAnnotations.get(sheet);
            if (curGenomicAnnotations == null) {
                curGenomicAnnotations = new ArrayList<>();
            }

            if (universalGenomicAnnotations != null) {
                curGenomicAnnotations.addAll(universalGenomicAnnotations);
            }

            // Define genomic annotations for this sheet
            List<VariantBasedGenomicAnnotation> curVariantAnnotations = variantAnnotations.get(sheet);
            if (curVariantAnnotations == null) {
                curVariantAnnotations = new ArrayList<>();
            }

            if (universalVariantAnnotations != null) {
                curVariantAnnotations.addAll(universalVariantAnnotations);
            }

            // Populate the sheet
            populateUncollapsedSnpAnntoationSheet(enrichmentWorkbook, sheet, targetVariants, curGenomicAnnotations, curVariantAnnotations, geneAnnotations);
        }

        // write
        enrichmentWorkbook.write(output.getAsFileOutputStream());

    }


    private void populateUncollapsedSnpAnntoationSheet(Workbook workbook,
                                                       String name,
                                                       Map<String, GeneticVariantInterval> targetVariants,
                                                       Collection<GenericGenomicAnnotation> genomicAnnotations,
                                                       Collection<VariantBasedGenomicAnnotation> variantAnnotations,
                                                       GeneBasedGenomicAnnotation geneAnnotations) {
        int numberOfCols = 6;
        int numberOfRows = targetVariants.size();

        // Determine number of columns
        for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
            numberOfCols += curAnnot.getHeader().length;
        }

        for (VariantBasedGenomicAnnotation curAnnot : variantAnnotations) {
            numberOfCols += curAnnot.getHeader().length;
        }

        if (geneAnnotations != null) {
            numberOfCols += 4 + geneAnnotations.getHeader().length;
        }

        // Init the sheet
        XSSFSheet variantOverview = initializeSheet(workbook, name, numberOfRows, numberOfCols);

        // Header
        setVariantBasedHeader(variantOverview, variantAnnotations, genomicAnnotations, geneAnnotations);

        // Populate rows
        int idNumber = 0;
        int r = 1; //+1 for header

        for (String variantId : targetVariants.keySet()) {
            GeneticVariantInterval curVariant = targetVariants.get(variantId);
            XSSFRow row = variantOverview.createRow(r);

            // Keeps track of column
            int c = 0;

            // Set fixed variant annotations
            c = fillVariantAnnotationCells(row, c, idNumber, curVariant);

            // Set dynamic variant annotations
            c = fillDynamicVariantBasedAnnotationCells(row, c, curVariant, variantAnnotations);

            // Genomic annotations, multiple per row
            c = fillDynamicGenomicAnnotationCells(variantOverview, c, r, idNumber, curVariant, genomicAnnotations, variantAnnotations, true);

            // Gene annotations, multiple per row
            if (geneAnnotations !=null) {
                c = fillGeneAnnotationCells(variantOverview, c, r, idNumber, curVariant, variantAnnotations, geneAnnotations, true);
            }

            idNumber++;
            r = variantOverview.getLastRowNum() + 1;
        }

        // Update the table size to actual row number
        for (XSSFTable curTable : variantOverview.getTables()){
            curTable.setArea(new AreaReference(
                    new CellReference(0, 0),
                    new CellReference(r-1, numberOfCols),
                    SpreadsheetVersion.EXCEL2007));
        }

        // Freeze the header and first row
        variantOverview.createFreezePane(1, 1);

        // Set the zoom level
        variantOverview.setZoom(75);

        // Size columns
        autoSizeColumns(variantOverview, numberOfCols - 1);
    }


    private int fillVariantAnnotationCells(XSSFRow row, int c, int id, GeneticVariantInterval curVariant) {
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
        XSSFCell a1Cell = row.createCell(c++, CellType.STRING);
        a1Cell.setCellValue(curVariant.getAllele1());
        a1Cell.setCellStyle(excelStyles.getBoldRightAlignedText());

        // Allele 2
        XSSFCell a2Cell = row.createCell(c++, CellType.STRING);
        a2Cell.setCellValue(curVariant.getAllele2());
        a2Cell.setCellStyle(excelStyles.getBoldRightAlignedText());

        return c;
    }

    private int fillDynamicVariantBasedAnnotationCells(XSSFRow row, int c, GeneticVariantInterval curVariant, Collection<VariantBasedGenomicAnnotation> variantAnnotations) {

        XSSFCell idCell = row.createCell(c++, CellType.STRING);

        // Variant annotations, only one per row
        for (VariantBasedGenomicAnnotation curAnnot : variantAnnotations) {
            VariantBasedGenomicAnnotationRecord curRec = curAnnot.query(curVariant.getPrimaryVariantId());

            // Lookup if any of the proxies have the info available
            LdProxy proxyUsed = null;
            if (curAnnot.useLdProxies()) {
                if (curRec == null && curVariant.hasLdProxies()) {
                    for (LdProxy proxy : curVariant.getLdOrderedLdProxies()) {
                        curRec = curAnnot.query(proxy.getVariantId());
                        if (curRec != null) {
                            proxyUsed = proxy;
                            break;
                        }
                    }
                }

                if (proxyUsed != null) {
                    if (!idCell.getStringCellValue().equals("")) {
                        idCell.setCellValue(idCell.getStringCellValue() + "|" + proxyUsed.toString());
                    } else {
                        idCell.setCellValue(proxyUsed.toString());
                    }
                }

            }

            // Fill the cells
            if (curRec != null) {
                for (int j = 0; j < curAnnot.getHeader().length; j++) {
                   // XSSFCell betaCell = row.createCell(c++, CellType.NUMERIC);
                   // betaCell.setCellStyle(excelStyles.getZscoreStyle());
                   // betaCell.setCellValue(curRec.getTextAnnotations().get(j));
                    setCellValueAutoTyped(row, c++, curRec.getTextAnnotations().get(j));
                }
            } else {
                for (int j = 0; j < curAnnot.getHeader().length; j++) {
                    row.createCell(c++, CellType.BLANK);
                }
            }

        }

        return c;
    }

    private int fillDynamicGenomicAnnotationCells(XSSFSheet variantOverview, int c, int r, int id, GeneticVariantInterval curVariant, Collection<GenericGenomicAnnotation> genomicAnnotations, Collection<VariantBasedGenomicAnnotation> variantAnnotations, boolean populateVariantAnnotationsInSubRows) {
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
                        //XSSFCell curCell = row.createCell(c + j, CellType.STRING);
                        //curCell.setCellValue(curRecord.getAnnotations().get(j));
                        setCellValueAutoTyped(row,c + j, curRecord.getAnnotations().get(j));

                        subRow++;
                    }

                    if (populateVariantAnnotationsInSubRows) {
                        int tmp = fillVariantAnnotationCells(row, 0, id, curVariant);
                        fillDynamicVariantBasedAnnotationCells(row, tmp, curVariant, variantAnnotations);
                    }
                }

            }
            c += curAnnot.getHeader().length;
        }

        return c;
    }

    private int fillGeneAnnotationCells(XSSFSheet variantOverview, int c, int r, int id, GeneticVariantInterval curVariant, Collection<VariantBasedGenomicAnnotation> variantAnnotations, GeneBasedGenomicAnnotation geneAnnotations, boolean populateVariantAnnotationsInSubRows) {
        Set<AnnotatedGene> curRecords = new HashSet<>(geneAnnotations.queryOverlapping(curVariant));
        AnnotatedGene closest = geneAnnotations.queryClosestTssWindow(curVariant, 1000000);
        curRecords.add(closest);

        int cOriginal = c;
        int subRow = 0;
        for (AnnotatedGene curRecord : curRecords) {

            if (curRecord == null) {
                continue;
            }

            XSSFRow row = variantOverview.getRow(r + subRow);
            if (row == null) {
                row = variantOverview.createRow(r + subRow);
            }
            c = cOriginal;

            // ID
            XSSFCell idCell = row.createCell(0, CellType.STRING);
            idCell.setCellValue(id);

            // Gene ID
            XSSFCell geneIdCell = row.createCell(c++, CellType.STRING);
            geneIdCell.setCellValue(curRecord.getGene());

            // Gene name
            XSSFCell geneNameCell = row.createCell(c++, CellType.STRING);
            geneNameCell.setCellValue(curRecord.getGeneSymbol());

            // Gene type
            XSSFCell geneTypeCell = row.createCell(c++, CellType.STRING);
            geneTypeCell.setCellValue(curRecord.getGeneType());

            // Gene overlap type
            XSSFCell geneOverlapTypeCell = row.createCell(c++, CellType.STRING);
            OverlapType curType = curRecord.determineOverlapType(curVariant);
            if (curRecord.equals(closest) && curType == OverlapType.EXTERNAL) {
                curType = OverlapType.CLOSEST_TSS;
            }

            if (curRecord.equals(closest)) {
                if (curType != OverlapType.CLOSEST_TSS) {
                    geneOverlapTypeCell.setCellValue(curType.toString() + ";" +OverlapType.CLOSEST_TSS.toString() );
                } else {
                    geneOverlapTypeCell.setCellValue(curType.toString());
                }
            } else {
                geneOverlapTypeCell.setCellValue(curType.toString());

            }

            if (populateVariantAnnotationsInSubRows) {
                int tmp = fillVariantAnnotationCells(row, 0, id, curVariant);
                fillDynamicVariantBasedAnnotationCells(row, tmp, curVariant, variantAnnotations);
            }

            // Dynamic cells
            for (int j = 0; j < geneAnnotations.getHeader().length; j++) {

                if (curRecord.getAnnotations().size() == geneAnnotations.getHeader().length) {
                    // ID
                    //idCell = row.createCell(0, CellType.STRING);
                    //idCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    //idCell.setCellValue(id);
                    //idCell.setCellType(CellType.STRING);

                    // Dynamic annotation
                    //XSSFCell curCell = row.createCell(c + j, CellType.STRING);
                    //curCell.setCellValue(curRecord.getAnnotations().get(j));
                    setCellValueAutoTyped(row, c + j, curRecord.getAnnotations().get(j));
                }
            }

            subRow++;

        }
        c += geneAnnotations.getHeader().length;

        return c;
    }

    private static void setVariantBasedHeader(XSSFSheet variantOverview,
                                              Collection<VariantBasedGenomicAnnotation> variantAnnotations,
                                              Collection<GenericGenomicAnnotation> genomicAnnotations,
                                              GeneBasedGenomicAnnotation geneAnnotations) {

        // Header
        XSSFRow headerRow = variantOverview.createRow(0);

        int hc = 0;
        headerRow.createCell(hc++, CellType.STRING).setCellValue("id");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("variant_id");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("chr");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("pos");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("ref");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("alt");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("ld_proxy");

        for (VariantBasedGenomicAnnotation curAnnot : variantAnnotations) {
            for (String curHeader : curAnnot.getHeader()) {
                headerRow.createCell(hc++, CellType.STRING).setCellValue(curHeader);
            }
        }

        for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
            for (String curHeader : curAnnot.getHeader()) {
                headerRow.createCell(hc++, CellType.STRING).setCellValue(curHeader);
            }
        }

        if (geneAnnotations != null) {
            headerRow.createCell(hc++, CellType.STRING).setCellValue("gene_id");
            headerRow.createCell(hc++, CellType.STRING).setCellValue("gene_name");
            headerRow.createCell(hc++, CellType.STRING).setCellValue("gene_type");
            headerRow.createCell(hc++, CellType.STRING).setCellValue("overlap_type");

            for (String curHeader : geneAnnotations.getHeader()) {
                headerRow.createCell(hc++, CellType.STRING).setCellValue(curHeader);
            }
        }
    }

    private static void autoSizeColumns(XSSFSheet variantOverview, int numberOfCols) {
        for (int c = 0; c < numberOfCols; c++) {
            variantOverview.autoSizeColumn(c);
            if (variantOverview.getColumnWidth(c) > 5000) {
                variantOverview.setColumnWidth(c, 5000);
            }
            variantOverview.setColumnWidth(c, variantOverview.getColumnWidth(c) + 200); //compensate for with auto filter and inaccuracies
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

    /**
     * Automatically determine the cell type and set the cell.
     * @param row
     * @param index
     * @param value
     */
    private void setCellValueAutoTyped(XSSFRow row, int index, String value) {

        try {
            double curValue = Double.parseDouble(value);
            XSSFCell cell = row.createCell(index, CellType.NUMERIC);
            cell.setCellValue(curValue);

            // If its < 1 > 1e-3
            if ((Math.abs(curValue) < 1) && (Math.abs(curValue) > 1e-3)) {
                cell.setCellStyle(excelStyles.getLargePvalueStyle());
                return;
            }

            // If its < 1 < 1e-3
            if ((Math.abs(curValue) < 1) && (Math.abs(curValue) < 1e-3)) {
                cell.setCellStyle(excelStyles.getSmallPvalueStyle());
                return;
            }

            // If it is an int
            if (curValue == Math.floor(curValue)) {
                cell.setCellStyle(excelStyles.getIntegerStyle());
                return;
            }

            // If it is a regular old double
            cell.setCellStyle(excelStyles.getZscoreStyle());

        } catch (NumberFormatException e) {

            if (value.toUpperCase(Locale.ROOT).equals("NA")) {
                XSSFCell cell = row.createCell(index, CellType.BLANK);
            } else {
                XSSFCell cell = row.createCell(index, CellType.STRING);
                cell.setCellValue(value);
            }

        }

    }

}
