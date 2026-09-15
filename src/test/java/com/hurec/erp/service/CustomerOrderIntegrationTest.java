package com.hurec.erp.service;

import com.hurec.erp.repository.CustomerOrderRepository;
import com.hurec.erp.dto.CustomerOrderDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@SpringBootTest
class CustomerOrderIntegrationTest {    // 테스트 메서드는 public 생략

    @Autowired
    private ExcelReaderService excelReaderService;

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Test
    @DisplayName("엑셀 파싱 후 DB 저장 통합 테스트")
    void readAndSaveExcelTest() throws Exception {
        File file = new File("C:/dev/Input/D0001_음처기T(권태용)_2026년08월.xlsx");
        FileInputStream fileInputStream = new FileInputStream(file);

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                file.getName(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileInputStream
        );

        List<CustomerOrderDto> dtoList = excelReaderService.parseCustomerOrders(mockFile);

        customerOrderService.saveCustomerOrders(dtoList);

        long savedCount = customerOrderRepository.count();
        System.out.println("==========================================");
        System.out.println("파싱된 DTO 건수: " + dtoList.size());
        System.out.println("DB 저장된 건수: " + savedCount);
        System.out.println("==========================================");

        assertThat(savedCount).isEqualTo(dtoList.size());
    }
}