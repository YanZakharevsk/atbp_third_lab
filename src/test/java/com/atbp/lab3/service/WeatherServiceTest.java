package com.atbp.lab3.service;

import com.atbp.lab3.model.WeatherResponse;
import com.atbp.lab3.services.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты WeatherService")
class WeatherServiceTest {

    private final WeatherService weatherService = new WeatherService();

    @ParameterizedTest
    @DisplayName("Проверка известных городов")
    @CsvSource({
            "minsk, -5.0",
            "gomel, -8.0",
            "vitebsk, 15.0",
            "grodno, 35.0",
            "brest, 40.0"
    })
    void testGetWeather_KnownCities(String city, double expectedTemp) {
        WeatherResponse response = weatherService.getWeather(city);

        assertEquals(city, response.getCity());
        assertEquals(expectedTemp, response.getTemperature(), 0.01);
    }

    @Test
    @DisplayName("Неизвестный город возвращает случайную температуру")
    void testGetWeather_UnknownCity() {
        WeatherResponse response = weatherService.getWeather("unknown_city");

        assertEquals("unknown_city", response.getCity());
        assertTrue(response.getTemperature() >= 15);
        assertTrue(response.getTemperature() <= 35);
    }

    @Test
    @DisplayName("Регистронезависимость названия города")
    void testGetWeather_CaseInsensitive() {
        WeatherResponse response1 = weatherService.getWeather("MINSK");
        WeatherResponse response2 = weatherService.getWeather("minsk");

        assertEquals(response1.getTemperature(), response2.getTemperature(), 0.01);
    }

    @Test
    @DisplayName("Проверка условия для города с температурой > 25")
    void testWeatherCondition_Sunny() {
        WeatherResponse response = weatherService.getWeather("grodno"); // 35°C
        assertEquals("sunny", response.getCondition());
    }
}