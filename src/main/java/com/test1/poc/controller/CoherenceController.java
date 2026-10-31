package com.test1.poc.controller;

import com.test1.poc.service.SwitchingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coherence")
public class CoherenceController {

    private final SwitchingService service;

    public CoherenceController(SwitchingService service) {

        this.service = service;
    }

    @PostMapping
    public String toggleCoherence(@RequestParam boolean enable) {

        return service.switchmech(enable);
    }

}
