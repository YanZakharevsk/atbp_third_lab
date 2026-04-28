package com.atbp.lab3.controller;

import com.atbp.lab3.model.WaterRequest;
import com.atbp.lab3.model.WaterResponse;
import com.atbp.lab3.services.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты WaterController")
class WaterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/water/norm - успешный расчет")
    void testCalculateWaterNorm_Success() throws Exception {
        when(weatherService.getWeather("minsk"))
                .thenReturn(new com.atbp.lab3.model.WeatherResponse("minsk", -5.0, "snowy"));

        WaterRequest request = new WaterRequest(70, 60, "minsk");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/water/norm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.waterNorm").value(2600.0))
                .andExpect(jsonPath("$.temperature").value(-5.0));
    }

    @Test
    @DisplayName("POST /api/water/norm - вес меньше 5 кг")
    void testCalculateWaterNorm_InvalidWeight() throws Exception {
        WaterRequest request = new WaterRequest(3, 60, "minsk");
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/water/norm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Weight must be between 5 and 250 kg"));
    }
}