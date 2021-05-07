/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.umcg.suresnp.pipeline.io;

import org.apache.poi.ss.usermodel.*;

/**
 *
 * @author patri
 */
public class ExcelStyles {

	private final CellStyle zscoreStyle;

	private final CellStyle largePvalueStyle;
	private final CellStyle smallPvalueStyle;

	private final CellStyle hlinkStyle;
	private final CellStyle boldStyle;

	private final CellStyle genomicPositionStyle;
	private final CellStyle boldGenomicPositionStyle;

	private final CellStyle rightAlignedText;
	private final CellStyle boldRightAlignedText;

	private final CellStyle subRowStyle;
	private final CellStyle integerStyle;

	public ExcelStyles(Workbook wb) {

		DataFormat format = wb.createDataFormat();

		//Also used for OR and AUC
		zscoreStyle = wb.createCellStyle();
		zscoreStyle.setDataFormat(format.getFormat("0.000"));

		largePvalueStyle = wb.createCellStyle();
		largePvalueStyle.setDataFormat(format.getFormat("0.000"));

		smallPvalueStyle = wb.createCellStyle();
		smallPvalueStyle.setDataFormat(format.getFormat("0.00E+0"));

		hlinkStyle = wb.createCellStyle();
		Font hlinkFont = wb.createFont();
		hlinkFont.setUnderline(Font.U_SINGLE);
		hlinkStyle.setFont(hlinkFont);

		boldStyle = wb.createCellStyle();
		Font fontBold = wb.createFont();
		fontBold.setBold(true);
		boldStyle.setFont(fontBold);

		genomicPositionStyle = wb.createCellStyle();
		genomicPositionStyle.setDataFormat(format.getFormat("###,###,##0"));

		integerStyle = wb.createCellStyle();
		integerStyle.setDataFormat(format.getFormat("#"));

		boldGenomicPositionStyle = wb.createCellStyle();
		fontBold.setFontHeightInPoints((short) 10);
		boldGenomicPositionStyle.setFont(fontBold);
		boldGenomicPositionStyle.setDataFormat(format.getFormat("###,###,##0"));

		rightAlignedText = wb.createCellStyle();
		rightAlignedText.setAlignment(HorizontalAlignment.RIGHT);

		boldRightAlignedText = wb.createCellStyle();
		boldRightAlignedText.setAlignment(HorizontalAlignment.RIGHT);
		boldRightAlignedText.setFont(fontBold);

		subRowStyle = wb.createCellStyle();
		subRowStyle.setBorderBottom(BorderStyle.NONE);
		subRowStyle.setBorderTop(BorderStyle.NONE);
		subRowStyle.setBorderLeft(BorderStyle.NONE);
		subRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		subRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);

	}

	/**
	 * Also used for OR and AUC
	 * 
	 * @return 
	 */
	public CellStyle getZscoreStyle() {
		return zscoreStyle;
	}

	public CellStyle getLargePvalueStyle() {
		return largePvalueStyle;
	}

	public CellStyle getSmallPvalueStyle() {
		return smallPvalueStyle;
	}

	public CellStyle getHlinkStyle() {
		return hlinkStyle;
	}

	public CellStyle getBoldStyle() {
		return boldStyle;
	}

	public CellStyle getGenomicPositionStyle() {
		return genomicPositionStyle;
	}

	public CellStyle getBoldGenomicPositionStyle() {
		return boldGenomicPositionStyle;
	}

	public CellStyle getRightAlignedText() {
		return rightAlignedText;
	}

	public CellStyle getBoldRightAlignedText() {
		return boldRightAlignedText;
	}

	public CellStyle getSubRowStyle() {
		return subRowStyle;
	}

	public CellStyle getIntegerStyle() {
		return integerStyle;
	}
}
