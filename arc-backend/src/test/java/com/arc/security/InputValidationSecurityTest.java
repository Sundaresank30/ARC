package com.arc.security;

import com.arc.dashboard.controller.DashboardController;
import com.arc.dashboard.service.DashboardService;
import com.arc.datapreparation.controller.DataPreparationController;
import com.arc.datapreparation.controller.SourceDocumentController;
import com.arc.datapreparation.dto.CreateProductionBatchRequest;
import com.arc.datapreparation.service.DataPreparationService;
import com.arc.datapreparation.service.SourceDocumentService;
import com.arc.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InputValidationSecurityTest {

    private MockMvc mockMvcSourceDoc;
    private MockMvc mockMvcDashboard;
    private MockMvc mockMvcDataPrep;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SourceDocumentService sourceDocumentService;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private DataPreparationService dataPreparationService;

    @BeforeEach
    void setUp() {
        mockMvcSourceDoc = MockMvcBuilders.standaloneSetup(new SourceDocumentController(sourceDocumentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvcDashboard = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvcDataPrep = MockMvcBuilders.standaloneSetup(new DataPreparationController(dataPreparationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("API 1: DELETE /api/source-documents/{id} with non-numeric ID should return 400 Bad Request")
    void testDeleteSourceDocument_InvalidId() throws Exception {
        mockMvcSourceDoc.perform(delete("/api/source-documents/invalid_string_id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 2: GET /api/source-documents/{id} with non-numeric ID should return 400 Bad Request")
    void testGetSourceDocumentById_InvalidId() throws Exception {
        mockMvcSourceDoc.perform(get("/api/source-documents/abc_99999999999999999999999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 3: POST /api/dashboard/carry-forward/{id}/resolve with non-numeric ID should return 400 Bad Request")
    void testResolveCarryForward_InvalidId() throws Exception {
        mockMvcDashboard.perform(post("/api/dashboard/carry-forward/not_an_integer/resolve"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 4: POST /api/dashboard/leakage-failures/{id}/resolve with non-numeric ID should return 400 Bad Request")
    void testResolveLeakageFailure_InvalidId() throws Exception {
        mockMvcDashboard.perform(post("/api/dashboard/leakage-failures/invalid_id_value/resolve"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 5: POST /api/data-preparation/batches with malformed JSON body should return 400 Bad Request")
    void testCreateProductionBatch_MalformedJson() throws Exception {
        mockMvcDataPrep.perform(post("/api/data-preparation/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"batchId\": \"B1\", \"partNoCount\": \"invalid_int\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 5: POST /api/data-preparation/batches with extremely long batchId should return 400 Bad Request")
    void testCreateProductionBatch_OversizedString() throws Exception {
        String longBatchId = "A".repeat(150);
        CreateProductionBatchRequest request = CreateProductionBatchRequest.builder()
                .batchId(longBatchId)
                .partNoSeries("PN001")
                .partNoCount(5)
                .serialNoSeries("SN001")
                .serialNoCount(5)
                .build();

        mockMvcDataPrep.perform(post("/api/data-preparation/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 6: POST /api/source-documents/upload with non-PDF file should return 400 Bad Request")
    void testUploadDocument_NonPdfFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.sh", "text/x-shellscript", "echo hello".getBytes());

        when(sourceDocumentService.processAndSavePdf(any(), any()))
                .thenThrow(new IllegalArgumentException("Uploaded file must be a PDF document"));

        mockMvcSourceDoc.perform(multipart("/api/source-documents/upload")
                        .file(file)
                        .param("batchId", "batch1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/data-preparation/batches returns JSON data safely without executing script tags")
    void testGetAllBatches_XssPayloadInBatchId() throws Exception {
        com.arc.datapreparation.dto.ProductionBatchResponse xssBatch = com.arc.datapreparation.dto.ProductionBatchResponse.builder()
                .id(1L)
                .batchId("<script>alert(1)</script>")
                .partNoSeries("PN001")
                .partNoCount(1)
                .serialNoSeries("SN001")
                .serialNoCount(1)
                .totalItems(1)
                .build();

        when(dataPreparationService.getAllBatches()).thenReturn(java.util.Collections.singletonList(xssBatch));

        mockMvcDataPrep.perform(get("/api/data-preparation/batches"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].batchId").value("<script>alert(1)</script>"));
    }

    @Test
    @DisplayName("Unhandled internal exception should return generic sanitized 500 message without exposing stack trace or internal SQL/Java details")
    void testGenericUnhandledException_ReturnsSanitized500() throws Exception {
        when(dataPreparationService.getAllBatches()).thenThrow(new RuntimeException("DB Connection Refused: postgresql://secret_db_host:5432/arc_db"));

        mockMvcDataPrep.perform(get("/api/data-preparation/batches"))
                .andExpect(status().isInternalServerError())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value(500))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message").value("An unexpected server error occurred. Please try again later."))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.trace").doesNotExist());
    }

    @Test
    @DisplayName("API 5: POST /api/data-preparation/batches with Unix path traversal in batchId should return 400 Bad Request")
    void testCreateProductionBatch_PathTraversalInBatchId() throws Exception {
        CreateProductionBatchRequest request = CreateProductionBatchRequest.builder()
                .batchId("../../etc/passwd")
                .partNoSeries("PN001")
                .partNoCount(5)
                .serialNoSeries("SN001")
                .serialNoCount(5)
                .build();

        mockMvcDataPrep.perform(post("/api/data-preparation/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 5: POST /api/data-preparation/batches with Windows path traversal in partNoSeries should return 400 Bad Request")
    void testCreateProductionBatch_PathTraversalInPartNoSeries() throws Exception {
        CreateProductionBatchRequest request = CreateProductionBatchRequest.builder()
                .batchId("Batch_1")
                .partNoSeries("..\\..\\Windows\\System32")
                .partNoCount(5)
                .serialNoSeries("SN001")
                .serialNoCount(5)
                .build();

        mockMvcDataPrep.perform(post("/api/data-preparation/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 5: POST /api/data-preparation/batches with SQL injection string payload should be rejected safely without modifying SQL structure")
    void testCreateProductionBatch_SqlInjectionPayload_RejectedByValidation() throws Exception {
        CreateProductionBatchRequest request = CreateProductionBatchRequest.builder()
                .batchId("John Doe AND 1=1")
                .partNoSeries("PN001")
                .partNoCount(5)
                .serialNoSeries("SN001")
                .serialNoCount(5)
                .build();

        mockMvcDataPrep.perform(post("/api/data-preparation/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 5: POST /api/data-preparation/batches with dangerous SQL syntax (' OR '1'='1) should be handled safely as invalid input")
    void testCreateProductionBatch_DangerousSqlSyntax_RejectedByValidation() throws Exception {
        CreateProductionBatchRequest request = CreateProductionBatchRequest.builder()
                .batchId("' OR '1'='1")
                .partNoSeries("PN001'; DROP TABLE production_batches; --")
                .partNoCount(5)
                .serialNoSeries("SN001")
                .serialNoCount(5)
                .build();

        mockMvcDataPrep.perform(post("/api/data-preparation/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/source-documents/upload with malformed file request returns sanitized 400 Bad Request without leaking internal exception classes")
    void testUploadSourceDocument_MalformedFile_ReturnsSanitizedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "invalid_part_name", "document.pdf", "application/pdf", "%PDF-1.4 test".getBytes());

        mockMvcSourceDoc.perform(multipart("/api/source-documents/upload")
                        .file(file)
                        .param("batchId", "batch1"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message").value("File upload request failed. Please check file size, format, and upload parameters."))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.trace").doesNotExist());
    }
}
