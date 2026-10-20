package com.hurec.erp.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

@Service
public class ExcelExportService {

    public void generateEngineerMonthlySheet(String filePath, String sheetName, List<String> headers, List<List<Object>> dataList) throws Exception {
        Workbook workbook = null;
        File file = new File(filePath);

        try {
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = WorkbookFactory.create(fis);
                }
            } else {
                workbook = new XSSFWorkbook();
            }

            workbook.setForceFormulaRecalculation(true);

            int existingIndex = workbook.getSheetIndex(sheetName);
            if (existingIndex != -1) {
                workbook.removeSheetAt(existingIndex);
            }

            Sheet sheet = workbook.createSheet(sheetName);

            int totalSheets = workbook.getNumberOfSheets();
            if (totalSheets > 1) {
                workbook.setSheetOrder(sheetName, totalSheets - 1);
            }

            DataFormat dataFormat = workbook.createDataFormat();

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(headerStyle);

            CellStyle defaultStyle = workbook.createCellStyle();
            setBorders(defaultStyle);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(dataFormat.getFormat("#,##0"));
            setBorders(numberStyle);

            CellStyle calcStyle = workbook.createCellStyle();
            calcStyle.setDataFormat(dataFormat.getFormat("#,##0"));
            setBorders(calcStyle);

            CellStyle yellowSummaryStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            yellowSummaryStyle.setFont(boldFont);
            yellowSummaryStyle.setDataFormat(dataFormat.getFormat("#,##0"));
            yellowSummaryStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            yellowSummaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(yellowSummaryStyle);

            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < headers.size(); c++) {
                Cell cell = headerRow.createCell(c);
                cell.setCellValue(headers.get(c));
                cell.setCellStyle(headerStyle);
            }

            int dataRowCount = dataList.size();
            for (int r = 0; r < dataRowCount; r++) {
                Row row = sheet.createRow(r + 1);
                List<Object> rowData = dataList.get(r);

                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.createCell(c);
                    Object val = (c < rowData.size()) ? rowData.get(c) : null;

                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                    } else if (val != null) {
                        cell.setCellValue(val.toString());
                    }

                    if (c >= 8 && c <= 15) {
                        cell.setCellStyle(numberStyle);
                    } else {
                        cell.setCellStyle(defaultStyle);
                    }
                }
            }

            int n = (dataRowCount > 0) ? dataRowCount + 1 : 1;

            int rN2 = n + 3; // n+3행
            int rN3 = n + 4; // n+4행
            int rN4 = n + 5; // n+5행
            int rN5 = n + 6; // n+6행
            int rN6 = n + 7; // n+7행

            Row rowN2 = sheet.createRow(rN2 - 1);
            createCell(rowN2, 7, "", defaultStyle, false);
            createCell(rowN2, 8, "", defaultStyle, false);
            createCell(rowN2, 9, "SUM(I2:I" + n + ")", calcStyle, true);

            Row rowN3 = sheet.createRow(rN3 - 1);
            createCell(rowN3, 7, "현장 수수료", defaultStyle, false);
            createCell(rowN3, 8, "COUNTIF(I2:I" + n + ", 0)", calcStyle, true);
            createCell(rowN3, 9, "-5000*I" + rN3, calcStyle, true);

            Row rowN4 = sheet.createRow(rN4 - 1);
            createCell(rowN4, 7, "현장 부가세", defaultStyle, false);
            createCell(rowN4, 8, "I" + rN3, calcStyle, true);
            createCell(rowN4, 9, "-6000*I" + rN3, calcStyle, true);

            Row rowN5 = sheet.createRow(rN5 - 1);
            createCell(rowN5, 7, "세금", defaultStyle, false);
            createCell(rowN5, 8, "3.3%", defaultStyle, false);
            createCell(rowN5, 9, "SUM(J" + rN2 + ":J" + rN4 + ")*-0.033", calcStyle, true);

            Row rowN6 = sheet.createRow(rN6 - 1);
            createCell(rowN6, 7, "", defaultStyle, false);
            createCell(rowN6, 8, "", defaultStyle, false);
            createCell(rowN6, 9, "SUM(J" + rN2 + ":J" + rN5 + ")", yellowSummaryStyle, true);

            CellRangeAddress calcTableRange = new CellRangeAddress(rN2 - 1, rN6 - 1, 7, 9);
            RegionUtil.setBorderTop(BorderStyle.MEDIUM, calcTableRange, sheet);
            RegionUtil.setBorderBottom(BorderStyle.MEDIUM, calcTableRange, sheet);
            RegionUtil.setBorderLeft(BorderStyle.MEDIUM, calcTableRange, sheet);
            RegionUtil.setBorderRight(BorderStyle.MEDIUM, calcTableRange, sheet);

            for (int c = 0; c < headers.size(); c++) {
                sheet.autoSizeColumn(c);
                int autoWidth = sheet.getColumnWidth(c);

                int maxCharWidth = getKoreanStringWidth(headers.get(c));
                for (List<Object> rowData : dataList) {
                    if (c < rowData.size() && rowData.get(c) != null) {
                        maxCharWidth = Math.max(maxCharWidth, getKoreanStringWidth(rowData.get(c).toString()));
                    }
                }
                int customWidth = (maxCharWidth + 3) * 256;
                int finalWidth = Math.min(Math.max(autoWidth + 1024, customWidth), 255 * 256);
                sheet.setColumnWidth(c, finalWidth);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private int getKoreanStringWidth(String str) {
        if (str == null) return 0;
        int length = 0;
        for (char ch : str.toCharArray()) {
            if (Character.getType(ch) == Character.OTHER_LETTER || (ch >= 0xAC00 && ch <= 0xD7A3)) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length;
    }

    private void createCell(Row row, int col, String val, CellStyle style, boolean isFormula) {
        Cell cell = row.createCell(col);
        if (isFormula) {
            if (val != null && val.startsWith("=")) {
                val = val.substring(1);
            }
            cell.setCellFormula(val);
        } else {
            cell.setCellValue(val);
        }
        cell.setCellStyle(style);
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}