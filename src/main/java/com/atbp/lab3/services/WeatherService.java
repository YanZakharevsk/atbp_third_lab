package com.atbp.lab3.services;

import com.atbp.lab3.WeatherResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class WeatherService {
    private static final Map<String, Double> weatherData = new HashMap<>();

    static{
        weatherData.put("minsk", -5.0);
        weatherData.put("gomel", -8.0);
        weatherData.put("vitebsk", 15.0);
        weatherData.put("grodno", 35.0);
        weatherData.put("brest", 40.0);
    }

    public WeatherResponse getWeather(String city) {

        String cityLower = city.toLowerCase();

        double temperature = weatherData.getOrDefault(cityLower, 15.0 + new Random().nextInt(20));

        String condition;
        if (temperature > 25) {
            condition = "sunny";
        } else if (temperature > 10) {
            condition = "cloudy";
        } else if (temperature > 0) {
            condition = "rainy";
        } else {
            condition = "snowy";
        }
        return new WeatherResponse(city,temperature,condition);
    }
}
