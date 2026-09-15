package com.hurec.erp.service;

import com.hurec.erp.domain.CustomerOrder;
import com.hurec.erp.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final CustomerOrderRepository customerOrderRepository;

    @Transactional
    public void uploadExcelFile(MultipartFile file) throws Exception {
        List<CustomerOrder> orders = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String seqNo = getCellValueAsString(row.getCell(0), evaluator);    // A열: N (순번)
                String workDate = getCellValueAsString(row.getCell(1), evaluator); // B열: 작업완료일

                if (seqNo.isEmpty() || workDate.isEmpty()) {
                    break;
                }

                String customerName = getCellValueAsString(row.getCell(3), evaluator); // D열: 고객성명
                String address = getCellValueAsString(row.getCell(4), evaluator);      // E열: 주소
                String serviceType = getCellValueAsString(row.getCell(5), evaluator);  // F열: 서비스구분
                String engineerName = getCellValueAsString(row.getCell(8), evaluator); // I열: 엔지니어명

                BigDecimal commissionFee = parseBigDecimal(row.getCell(9), evaluator);  // J열: 도급비
                BigDecimal supplyPrice = parseBigDecimal(row.getCell(14), evaluator);   // O열: 공급가액
                BigDecimal vat = parseBigDecimal(row.getCell(15), evaluator);           // P열: 부가세
                BigDecimal totalAmount = parseBigDecimal(row.getCell(16), evaluator);    // Q열: 합계(부가세포함)

                CustomerOrder order = CustomerOrder.builder()
                        .workDate(workDate)
                        .customerName(customerName)
                        .address(address)
                        .serviceType(serviceType)
                        .engineerName(engineerName)
                        .commissionFee(commissionFee)
                        .supplyPrice(supplyPrice)
                        .vat(vat)
                        .totalAmount(totalAmount)
                        .build();

                orders.add(order);
            }
        }

        customerOrderRepository.saveAll(orders);
    }

    private String getCellValueAsString(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> formatNumericCell(cell.getNumericCellValue(), DateUtil.isCellDateFormatted(cell) ? cell : null);
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                CellValue cellValue = evaluator.evaluate(cell);
                if (cellValue == null) yield "";
                yield switch (cellValue.getCellType()) {
                    case STRING -> cellValue.getStringValue().trim();
                    case NUMERIC -> formatNumericCell(cellValue.getNumberValue(), null);
                    case BOOLEAN -> String.valueOf(cellValue.getBooleanValue());
                    default -> "";
                };
            }
            default -> "";
        };
    }

    private String formatNumericCell(double value, Cell dateCell) {
        if (dateCell != null && DateUtil.isCellDateFormatted(dateCell)) {
            return dateCell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private BigDecimal parseBigDecimal(Cell cell, FormulaEvaluator evaluator) {
        String val = getCellValueAsString(cell, evaluator);
        if (val.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(val.replace(",", ""));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}