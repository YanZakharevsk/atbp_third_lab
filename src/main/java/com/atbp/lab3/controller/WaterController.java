package com.atbp.lab3.controller;

import com.atbp.lab3.WaterRequest;
import com.atbp.lab3.services.WaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water")
public class WaterController {

    private final WaterService waterService;

    public WaterController(WaterService waterService){
        this.waterService = waterService;
    }

    @PostMapping("/norm")
    public ResponseEntity<?> calculateWaterNorm(@RequestBody WaterRequest waterRequest){
       return waterService.calculateWaterNorm(waterRequest);
    }

}
