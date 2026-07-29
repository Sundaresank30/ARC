package com.arc.auth.controller;

import com.arc.auth.dto.LoginRequest;
import com.arc.auth.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_withManagerRole_returnsTokenAndModules() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.modules").isArray())
                .andExpect(jsonPath("$.modules.length()").value(3))
                .andExpect(jsonPath("$.modules[0]").value("Dashboard"))
                .andExpect(jsonPath("$.modules[1]").value("Data Preparation"))
                .andExpect(jsonPath("$.modules[2]").value("Settings"));
    }

    @Test
    void login_withOperatorRole_returnsTokenAndModules() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("OPERATOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("OPERATOR"))
                .andExpect(jsonPath("$.modules.length()").value(4))
                .andExpect(jsonPath("$.modules[0]").value("Data Embossing"))
                .andExpect(jsonPath("$.modules[1]").value("Leakage Testing"))
                .andExpect(jsonPath("$.modules[2]").value("Machine"))
                .andExpect(jsonPath("$.modules[3]").value("Settings"));
    }

    @Test
    void login_withInvalidRole_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid role selected"));
    }

    @Test
    void login_withBlankRole_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Role is required"));
    }

    @Test
    void me_withValidToken_returnsRoleAndModules() throws Exception {
        String token = loginAndExtractToken("MANAGER");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.modules.length()").value(3));
    }

    @Test
    void me_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    private String loginAndExtractToken(String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(role))))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class);

        assertThat(response.getToken()).isNotBlank();
        return response.getToken();
    }
}
