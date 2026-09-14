package com.hurec.erp.controller;

import com.hurec.erp.dto.CustomerOrderDto;
import com.hurec.erp.service.CustomerOrderService;
import com.hurec.erp.service.ExcelReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final ExcelReaderService excelReaderService;
    private final CustomerOrderService customerOrderService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file")MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "업로드할 엑셀 파일을 선택해주세요."));
        }

        try {
            List<CustomerOrderDto> dtoList = excelReaderService.parseCustomerOrders(file);

            int savedCount = customerOrderService.saveCustomerOrders(dtoList);

            return ResponseEntity.ok(Map.of(
                    "message", "엑셀 업로드 및 DB 저장이 완료되었습니다.",
                    "totalParsed", dtoList.size(),
                    "totalSaved", savedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "엑셀 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
}
