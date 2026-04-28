package com.atbp.lab3.service;

import com.atbp.lab3.model.WaterRequest;
import com.atbp.lab3.model.WaterResponse;
import com.atbp.lab3.model.WeatherResponse;
import com.atbp.lab3.services.WaterService;
import com.atbp.lab3.services.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты WaterService")
class WaterServiceTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WaterService waterService;

    private WaterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new WaterRequest(70, 60, "minsk");
    }


    @Test
    @DisplayName("Позитивный: корректный расчет нормы воды при нормальной температуре")
    void testCalculateWaterNorm_Success_NormalTemperature() {
        when(weatherService.getWeather("minsk"))
                .thenReturn(new WeatherResponse("minsk", -5.0, "snowy"));

        ResponseEntity<?> response = waterService.calculateWaterNorm(validRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof WaterResponse);

        WaterResponse body = (WaterResponse) response.getBody();
        assertEquals("success", body.getStatus());
        assertEquals(2600.0, body.getWaterNorm(), 0.01);
        assertEquals(-5.0, body.getTemperature());
        assertEquals("Normal calculation", body.getMessage());

        verify(weatherService, times(1)).getWeather("minsk");
    }

    @Test
    @DisplayName("Позитивный: при температуре > 30°C норма увеличивается на 20%")
    void testCalculateWaterNorm_Success_HotWeather() {
        when(weatherService.getWeather("grodno"))
                .thenReturn(new WeatherResponse("grodno", 35.0, "sunny"));

        WaterRequest hotRequest = new WaterRequest(70, 60, "grodno");
        ResponseEntity<?> response = waterService.calculateWaterNorm(hotRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        WaterResponse body = (WaterResponse) response.getBody();

        // 30*70 = 2100 + 500 = 2600, +20% = 3120
        assertEquals(3120.0, body.getWaterNorm(), 0.01);
        assertEquals("Increased due to hot weather", body.getMessage());

        verify(weatherService, times(1)).getWeather("grodno");
    }

    @Test
    @DisplayName("Позитивный: разное время активности")
    void testCalculateWaterNorm_DifferentActivityTimes() {
        when(weatherService.getWeather(anyString()))
                .thenReturn(new WeatherResponse("minsk", -5.0, "snowy"));

        WaterRequest request0 = new WaterRequest(70, 0, "minsk");
        ResponseEntity<?> response0 = waterService.calculateWaterNorm(request0);
        assertEquals(2100.0, ((WaterResponse) response0.getBody()).getWaterNorm(), 0.01);

        WaterRequest request30 = new WaterRequest(70, 30, "minsk");
        ResponseEntity<?> response30 = waterService.calculateWaterNorm(request30);
        assertEquals(2350.0, ((WaterResponse) response30.getBody()).getWaterNorm(), 0.01);

        // 120 минут активности
        WaterRequest request120 = new WaterRequest(70, 120, "minsk");
        ResponseEntity<?> response120 = waterService.calculateWaterNorm(request120);
        assertEquals(3100.0, ((WaterResponse) response120.getBody()).getWaterNorm(), 0.01);
    }


    @Test
    @DisplayName("Граничное значение: минимальный вес 5 кг")
    void testBoundaryWeight_Min() {
        when(weatherService.getWeather(anyString()))
                .thenReturn(new WeatherResponse("minsk", -5.0, "snowy"));

        WaterRequest request = new WaterRequest(5, 60, "minsk");
        ResponseEntity<?> response = waterService.calculateWaterNorm(request);

        // 30*5 = 150 + 500 = 650
        assertEquals(650.0, ((WaterResponse) response.getBody()).getWaterNorm(), 0.01);
    }

    @Test
    @DisplayName("Граничное значение: максимальный вес 250 кг")
    void testBoundaryWeight_Max() {
        when(weatherService.getWeather(anyString()))
                .thenReturn(new WeatherResponse("minsk", -5.0, "snowy"));

        WaterRequest request = new WaterRequest(250, 60, "minsk");
        ResponseEntity<?> response = waterService.calculateWaterNorm(request);

        // 30*250 = 7500 + 500 = 8000
        assertEquals(8000.0, ((WaterResponse) response.getBody()).getWaterNorm(), 0.01);
    }

    @Test
    @DisplayName("Граничное значение: температура ровно 30°C - без увеличения")
    void testBoundaryTemperature_Exactly30() {
        when(weatherService.getWeather("city30"))
                .thenReturn(new WeatherResponse("city30", 30.0, "warm"));

        WaterRequest request = new WaterRequest(70, 60, "city30");
        ResponseEntity<?> response = waterService.calculateWaterNorm(request);

        // 30*70 = 2100 + 500 = 2600, без увеличения (только >30)
        assertEquals(2600.0, ((WaterResponse) response.getBody()).getWaterNorm(), 0.01);
        assertEquals("Normal calculation", ((WaterResponse) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Граничное значение: температура 30.1°C - увеличение")
    void testBoundaryTemperature_30_1() {
        when(weatherService.getWeather("city30"))
                .thenReturn(new WeatherResponse("city30", 30.1, "warm"));

        WaterRequest request = new WaterRequest(70, 60, "city30");
        ResponseEntity<?> response = waterService.calculateWaterNorm(request);

        // 2600 * 1.2 = 3120
        assertEquals(3120.0, ((WaterResponse) response.getBody()).getWaterNorm(), 0.01);
        assertEquals("Increased due to hot weather", ((WaterResponse) response.getBody()).getMessage());
    }


    @Test
    @DisplayName("Негативный: вес меньше 5 кг")
    void testCalculateWaterNorm_WeightLessThanMin() {
        WaterRequest invalidRequest = new WaterRequest(4.9, 60, "minsk");
        ResponseEntity<?> response = waterService.calculateWaterNorm(invalidRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        WaterResponse body = (WaterResponse) response.getBody();
        assertEquals("error", body.getStatus());
        assertEquals(0.0, body.getWaterNorm(), 0.01);
        assertEquals("Weight must be between 5 and 250 kg", body.getMessage());

        verify(weatherService, never()).getWeather(anyString());
    }

    @Test
    @DisplayName("Негативный: вес больше 250 кг")
    void testCalculateWaterNorm_WeightGreaterThanMax() {
        WaterRequest invalidRequest = new WaterRequest(251, 60, "minsk");
        ResponseEntity<?> response = waterService.calculateWaterNorm(invalidRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        WaterResponse body = (WaterResponse) response.getBody();
        assertEquals("error", body.getStatus());
        assertEquals("Weight must be between 5 and 250 kg", body.getMessage());

        verify(weatherService, never()).getWeather(anyString());
    }

    @Test
    @DisplayName("Негативный: отрицательное время активности")
    void testCalculateWaterNorm_NegativeActivity() {
        WaterRequest invalidRequest = new WaterRequest(70, -10, "minsk");
        ResponseEntity<?> response = waterService.calculateWaterNorm(invalidRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        WaterResponse body = (WaterResponse) response.getBody();
        assertEquals("error", body.getStatus());
        assertEquals("Activity minutes cannot be negative", body.getMessage());

        verify(weatherService, never()).getWeather(anyString());
    }

    @Test
    @DisplayName("Негативный: вес 0 кг")
    void testCalculateWaterNorm_WeightZero() {
        WaterRequest invalidRequest = new WaterRequest(0, 60, "minsk");
        ResponseEntity<?> response = waterService.calculateWaterNorm(invalidRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        WaterResponse body = (WaterResponse) response.getBody();
        assertTrue(body.getMessage().contains("between 5 and 250"));
    }


    @ParameterizedTest
    @DisplayName("Параметризованный тест: разные комбинации веса, активности и города")
    @CsvSource({
            "70, 60, minsk, -5, 2600, Normal calculation",
            "70, 60, grodno, 35, 3120, Increased due to hot weather",
            "70, 60, brest, 40, 3120, Increased due to hot weather",
            "70, 60, vitebsk, 15, 2600, Normal calculation",
            "50, 0, minsk, -5, 1500, Normal calculation",
            "50, 60, brest, 40, 2400, Increased due to hot weather",
            "100, 120, minsk, -5, 4000, Normal calculation",
            "100, 60, grodno, 35, 4200, Increased due to hot weather"
    })
    void testParameterizedWaterNorm(double weight, int activity, String city,
                                    double temp, double expectedNorm, String expectedMessage) {
        when(weatherService.getWeather(city))
                .thenReturn(new WeatherResponse(city, temp, "test"));

        WaterRequest request = new WaterRequest(weight, activity, city);
        ResponseEntity<?> response = waterService.calculateWaterNorm(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        WaterResponse body = (WaterResponse) response.getBody();
        assertEquals(expectedNorm, body.getWaterNorm(), 0.01);
        assertEquals(expectedMessage, body.getMessage());
    }

    @Test
    @DisplayName("Проверка: WeatherService вызывается ровно один раз")
    void testWeatherServiceCalledExactlyOnce() {
        when(weatherService.getWeather("minsk"))
                .thenReturn(new WeatherResponse("minsk", -5.0, "snowy"));

        waterService.calculateWaterNorm(validRequest);

        verify(weatherService, times(1)).getWeather("minsk");
        verifyNoMoreInteractions(weatherService);
    }

    @Test
    @DisplayName("Проверка: при ошибке валидации WeatherService не вызывается")
    void testWeatherServiceNotCalledOnValidationError() {
        WaterRequest invalidRequest = new WaterRequest(3, 60, "minsk");
        waterService.calculateWaterNorm(invalidRequest);

        verify(weatherService, never()).getWeather(anyString());
    }

    @Test
    @DisplayName("Проверка: округление до 2 знаков после запятой")
    void testRoundingToTwoDecimals() {
        when(weatherService.getWeather("minsk"))
                .thenReturn(new WeatherResponse("minsk", -5.0, "snowy"));

        WaterRequest request = new WaterRequest(70, 30, "minsk");
        ResponseEntity<?> response = waterService.calculateWaterNorm(request);

        // 2350.0 должно остаться 2350.0
        assertEquals(2350.0, ((WaterResponse) response.getBody()).getWaterNorm(), 0.01);
    }
}