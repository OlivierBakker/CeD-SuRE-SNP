package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.util.IntervalTreeMap;
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
import org.apache.xmlbeans.impl.xb.xsdschema.Annotated;

import java.io.IOException;
import java.util.*;

public class ExcelWriter {

    private GenericFile output;
    private ExcelStyles excelStyles;

    public ExcelWriter(GenericFile output) {
        this.output = output;
    }

    public void saveSnpAnnotationExcel(Map<String, GeneticVariantInterval> targetVariants,
                                       Map<String, List<GenericGenomicAnnotation>> genomicAnnotations,
                                       Map<String, List<VariantBasedNumericGenomicAnnotation>> variantAnnotations,
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
        List<VariantBasedNumericGenomicAnnotation> universalVariantAnnotations = null;
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
            List<VariantBasedNumericGenomicAnnotation> curVariantAnnotations = variantAnnotations.get(sheet);
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
                                                       Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations,
                                                       GeneBasedGenomicAnnotation geneAnnotations) {
        int numberOfCols = 6;
        int numberOfRows = targetVariants.size();

        // Determine number of columns
        for (GenericGenomicAnnotation curAnnot : genomicAnnotations) {
            numberOfCols += curAnnot.getHeader().length;
        }

        for (VariantBasedNumericGenomicAnnotation curAnnot : variantAnnotations) {
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
                    new CellReference(r, numberOfCols),
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
        XSSFCell a1Cell = row.createCell(c++, CellType.NUMERIC);
        a1Cell.setCellValue(curVariant.getAllele1());
        a1Cell.setCellStyle(excelStyles.getBoldStyle());

        // Allele 2
        XSSFCell a2Cell = row.createCell(c++, CellType.NUMERIC);
        a2Cell.setCellValue(curVariant.getAllele2());
        a2Cell.setCellStyle(excelStyles.getBoldStyle());

        return c;
    }

    private int fillDynamicVariantBasedAnnotationCells(XSSFRow row, int c, GeneticVariantInterval curVariant, Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations) {

        XSSFCell idCell = row.createCell(c++, CellType.STRING);

        // Variant annotations, only one per row
        for (VariantBasedNumericGenomicAnnotation curAnnot : variantAnnotations) {
            VariantBasedNumericGenomicAnnotationRecord curRec = curAnnot.query(curVariant.getPrimaryVariantId());

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

    private int fillDynamicGenomicAnnotationCells(XSSFSheet variantOverview, int c, int r, int id, GeneticVariantInterval curVariant, Collection<GenericGenomicAnnotation> genomicAnnotations, Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations, boolean populateVariantAnnotationsInSubRows) {
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
                        int tmp = fillVariantAnnotationCells(row, 0, id, curVariant);
                        fillDynamicVariantBasedAnnotationCells(row, tmp, curVariant, variantAnnotations);
                    }
                }

            }
            c += curAnnot.getHeader().length;
        }

        return c;
    }

    private int fillGeneAnnotationCells(XSSFSheet variantOverview, int c, int r, int id, GeneticVariantInterval curVariant, Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations, GeneBasedGenomicAnnotation geneAnnotations, boolean populateVariantAnnotationsInSubRows) {
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

            // Dynamic cells
            for (int j = 0; j < geneAnnotations.getHeader().length; j++) {

                if (curRecord.getAnnotations().size() == geneAnnotations.getHeader().length) {
                    // ID
                    XSSFCell idCell = row.createCell(0, CellType.STRING);
                    idCell.setCellValue(id);

                    // Dynamic annotation
                    XSSFCell curCell = row.createCell(c + j, CellType.STRING);
                    curCell.setCellValue(curRecord.getAnnotations().get(j));
                }

                if (populateVariantAnnotationsInSubRows) {
                    int tmp = fillVariantAnnotationCells(row, 0, id, curVariant);
                    fillDynamicVariantBasedAnnotationCells(row, tmp, curVariant, variantAnnotations);
                }
            }

            subRow++;

        }
        c += geneAnnotations.getHeader().length;

        return c;
    }


    private static void setVariantBasedHeader(XSSFSheet variantOverview,
                                              Collection<VariantBasedNumericGenomicAnnotation> variantAnnotations,
                                              Collection<GenericGenomicAnnotation> genomicAnnotations,
                                              GeneBasedGenomicAnnotation geneAnnotations) {

        // Header
        XSSFRow headerRow = variantOverview.createRow(0);

        int hc = 0;
        headerRow.createCell(hc++, CellType.STRING).setCellValue("id");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("variant_id");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("chromosome");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("position");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("allele_1");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("allele_2");
        headerRow.createCell(hc++, CellType.STRING).setCellValue("ld_proxy");

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
}
