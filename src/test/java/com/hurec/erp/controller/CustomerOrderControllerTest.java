package com.hurec.erp.controller;

import com.hurec.erp.ErpApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ErpApplication.class) // 메인 애플리케이션 클래스 명시
@AutoConfigureMockMvc
class CustomerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("엑셀 파일 업로드 및 DB 저장 REST API 검증")
    void uploadExcelFileTest() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("C:\\dev\\Input\\D0001_음처기T(권태용)_2026년08월.xlsx");

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "D0001_음처기T(권태용)_2026년08월.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileInputStream
        );

        mockMvc.perform(multipart("/api/orders/upload").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalParsed", greaterThan(0)))
                .andExpect(jsonPath("$.totalSaved", greaterThan(0)));
    }
}