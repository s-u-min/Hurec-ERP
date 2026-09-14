package com.hurec.erp.service;

import com.hurec.erp.dto.CustomerOrderDto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelReaderService {

    public List<CustomerOrderDto> parseCustomerOrders(MultipartFile file) throws Exception {
        List<CustomerOrderDto> orderList = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet("② 고객명단");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            DataFormatter formatter = new DataFormatter();

            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String customerName = formatter.formatCellValue(row.getCell(3));
                if (customerName.contains("합계") || customerName.trim().isEmpty()) {
                    break;
                }

                String workDate = formatter.formatCellValue(row.getCell(1));
                String address = formatter.formatCellValue(row.getCell(4));
                String serviceType = formatter.formatCellValue(row.getCell(5));
                String engineerName = formatter.formatCellValue(row.getCell(8));

                BigDecimal commissionFee = parseBigDecimal(formatter.formatCellValue(row.getCell(9)));
                BigDecimal supplyPrice = parseBigDecimal(formatter.formatCellValue(row.getCell(14)));
                BigDecimal vat = parseBigDecimal(formatter.formatCellValue(row.getCell(15)));
                BigDecimal totalAmount = parseBigDecimal(formatter.formatCellValue(row.getCell(16)));

                CustomerOrderDto dto = CustomerOrderDto.builder()
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

                orderList.add(dto);
            }
        }
        return orderList;
    }
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        String cleaned = value.replaceAll("[^0-9.-]", "").trim();
        if (cleaned.isEmpty() || cleaned.equals("-")) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}