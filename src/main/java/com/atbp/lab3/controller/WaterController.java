package com.atbp.lab3.controller;

import com.atbp.lab3.model.WaterRequest;
import com.atbp.lab3.services.WaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
