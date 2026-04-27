package com.atbp.lab3.services;
import com.atbp.lab3.model.WaterRequest;
import com.atbp.lab3.model.WaterResponse;
import com.atbp.lab3.model.WeatherResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WaterService {

    private WeatherService weatherService;

    public WaterService(WeatherService weatherService){
        this.weatherService = weatherService;
    }

    public ResponseEntity<?> calculateWaterNorm(WaterRequest waterRequest){
        try{
            if(waterRequest.getWeight() < 5 || waterRequest.getWeight() > 250){
                WaterResponse errorResponse =  new WaterResponse("error", 0, 0, "Weight must be between 5 and 250 kg");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if(waterRequest.getActivityMinutes() < 0){
                WaterResponse errorResponse =  new WaterResponse("error", 0, 0, "Activity minutes cannot be negative");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            double temperature = getTemperatureForCity(waterRequest.getCity());
            double baseNorm = 30 * waterRequest.getWeight();
            double activityExtra = (waterRequest.getActivityMinutes() / 60.0) * 500;
            double totalNorm = baseNorm + activityExtra;

            boolean isHot = temperature > 30;
            if(isHot) totalNorm *= 1.2;

            totalNorm = Math.round(totalNorm * 100) / 100.0;

            WaterResponse waterResponse =  new WaterResponse("success", totalNorm, temperature, isHot ? "Increased due to hot weather" : "Normal calculation");
            return ResponseEntity.ok(waterResponse);
        }catch (Exception e){
            WaterResponse errorResponse =  new WaterResponse("error", 0, 0, "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private double getTemperatureForCity(String city){
        WeatherResponse weatherResponse = weatherService.getWeather(city);
        return weatherResponse.getTemperature();
    }
}
