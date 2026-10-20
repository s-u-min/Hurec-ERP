package com.hurec.erp.service;

import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExcelExportServiceTest {

    private final ExcelExportService excelExportService = new ExcelExportService();

    @Test
    void exportAllEngineersTest() throws Exception {
        String inputDirPath = "C:\\dev\\Input";
        String outputDirPath = "C:\\dev\\Output";

        File inputDir = new File(inputDirPath);
        File outputDir = new File(outputDirPath);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] targetFiles = inputDir.listFiles((dir, name) -> name.startsWith("음처기T") && name.endsWith(".xlsx"));

        if (targetFiles == null || targetFiles.length == 0) {
            throw new IllegalStateException("C:\\dev\\Input 폴더 내에 '음처기T'로 시작하는 원본 엑셀 파일이 존재하지 않습니다.");
        }

        File sourceFile = targetFiles[0];
        String sourceFileName = sourceFile.getName();
        System.out.println("감지된 원본 파일명: " + sourceFileName);

        String sheetName = extractSheetNameFromFileName(sourceFileName);
        System.out.println("생성할 시트명: " + sheetName);

        List<String> headers = new ArrayList<>();
        Map<String, List<List<Object>>> engineerDataMap = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(sourceFile);
             Workbook workbook = WorkbookFactory.create(fis)) {

            if (workbook.getNumberOfSheets() < 2) {
                throw new IllegalArgumentException("엑셀 파일에 시트가 최소 2개 이상 존재해야 합니다.");
            }

            Sheet sheet = workbook.getSheetAt(1);

            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                int lastCellNum = headerRow.getLastCellNum();
                for (int c = 0; c < lastCellNum; c++) {
                    Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    headers.add(getCellValueAsString(cell));
                }
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Cell engineerCell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String engineerName = getCellValueAsString(engineerCell).trim();

                if (engineerName.isEmpty() || "합계".equals(engineerName)) {
                    continue;
                }

                List<Object> rowData = new ArrayList<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData.add(getCellValue(cell));
                }

                engineerDataMap.computeIfAbsent(engineerName, k -> new ArrayList<>()).add(rowData);
            }
        }

        System.out.println("감지된 전체 엔지니어 수: " + engineerDataMap.size() + "명");

        for (Map.Entry<String, List<List<Object>>> entry : engineerDataMap.entrySet()) {
            String engineerName = entry.getKey();
            List<List<Object>> engineerDataList = entry.getValue();

            String outputFileName = engineerName + ".xlsx";
            String targetFilePath = new File(outputDir, outputFileName).getAbsolutePath();

            excelExportService.generateEngineerMonthlySheet(targetFilePath, sheetName, headers, engineerDataList);
            System.out.println("[" + outputFileName + "] 생성 완료 (" + engineerDataList.size() + "건)");
        }

        System.out.println("전체 엔지니어 파일 파이프라인 처리 완료!");
    }

    private String extractSheetNameFromFileName(String fileName) {
        Pattern pattern = Pattern.compile("\\d{2}(\\d{2})(\\d{2})");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            String yy = matcher.group(1);
            String mm = matcher.group(2);
            return yy + mm;
        }
        throw new IllegalArgumentException("파일명에서 연월 정보를 찾을 수 없습니다: " + fileName);
    }

    private Object getCellValue(Cell cell) {
        if (cell == null) return null;

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        return switch (type) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                yield cell.getNumericCellValue();
            }
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            default -> "";
        };
    }

    private String getCellValueAsString(Cell cell) {
        Object val = getCellValue(cell);
        return val == null ? "" : String.valueOf(val);
    }
}