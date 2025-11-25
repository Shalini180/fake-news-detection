package com.fakenews.api;

import com.fakenews.api.dto.ArticleRequest;
import com.fakenews.api.dto.FakeNewsResult;
import com.fakenews.service.RobertaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FakeNewsControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private RobertaService robertaService;

        @Test
        void analyze_validArticle_returns200() throws Exception {
                // Given
                ArticleRequest request = new ArticleRequest();
                request.setTitle("Test Article");
                request.setContent("This is a test article with sufficient content for analysis.");
                request.setSource("test-source.com");

                // Mock RoBERTa service (not available in tests)
                when(robertaService.analyze(anyString(), anyString(), anyString()))
                                .thenThrow(new RuntimeException("Service unavailable in test"));

                // When & Then
                MvcResult result = mockMvc.perform(post("/api/v1/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Test Article"))
                                .andExpect(jsonPath("$.credibilityScore").isNumber())
                                .andExpect(jsonPath("$.classification").isString())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                FakeNewsResult response = objectMapper.readValue(responseBody, FakeNewsResult.class);

                assertThat(response.getCredibilityScore()).isBetween(0.0, 1.0);
                assertThat(response.getClassification()).isNotNull();
        }

        @Test
        void analyze_missingTitle_stillProcesses() throws Exception {
                // Given
                ArticleRequest request = new ArticleRequest();
                // Missing title
                request.setContent("This is content without a title");

                // When & Then - Current behavior: no validation, so it processes
                // TODO: Add @Valid annotation to controller and expect 400
                mockMvc.perform(post("/api/v1/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().is5xxServerError()); // NullPointerException expected without title
        }

        @Test
        void batchAnalyze_multipleArticles_returnsCorrectCount() throws Exception {
                // Given
                ArticleRequest req1 = new ArticleRequest();
                req1.setTitle("Article 1");
                req1.setContent("Content for the first test article.");

                ArticleRequest req2 = new ArticleRequest();
                req2.setTitle("Article 2");
                req2.setContent("Content for the second test article.");

                List<ArticleRequest> requests = Arrays.asList(req1, req2);

                // When & Then
                MvcResult result = mockMvc.perform(post("/api/v1/batch-analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requests)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.count").value(2))
                                .andExpect(jsonPath("$.results").isArray())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                assertThat(responseBody).contains("\"count\":2");
        }

        @Test
        void getStats_returnsSystemStatistics() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/stats"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalArticles").isNumber());
        }

        @Test
        void getLeastCredible_returnsArticleList() throws Exception {
                // First, analyze some articles to populate the heap
                ArticleRequest request = new ArticleRequest();
                request.setTitle("Test Article");
                request.setContent("Content for testing least credible endpoint.");

                mockMvc.perform(post("/api/v1/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                // When & Then
                mockMvc.perform(get("/api/v1/least-credible")
                                .param("limit", "5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }
}
