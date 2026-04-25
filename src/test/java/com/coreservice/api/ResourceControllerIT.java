package com.coreservice.api;

import com.coreservice.api.dto.ResourceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Base64;

@Import(TestUsersConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ResourceControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void shouldCreateResource() throws Exception {
        var request = new ResourceRequest("Test", "Description");

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(
                post("/resources")
                    .header("Authorization", "Basic " + basic)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void shouldReturnBadRequestWhenInvalid() throws Exception {
        var request = new ResourceRequest("", "desc");

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Basic " + basic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        String basic = Base64.getEncoder().encodeToString("other:password".getBytes());

        mockMvc.perform(get("/resources")
                .header("Authorization", "Basic " + basic))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("errorResponse", "unauthorized"));
    }

    @Test
    void shouldReturnBadRequestWhenIdIsNotUUID() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(get("/resources/not-a-uuid")
        .header("Authorization", "Basic " + basic))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenIdIsNull() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(get("/resources/")
                .header("Authorization", "Basic " + basic))
                .andExpect(status().isNotFound()); // ou 400 selon ton mapping
    }
}
