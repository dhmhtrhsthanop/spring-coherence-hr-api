package com.test1.poc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SwitchingService {

    @Value("${spring.cache.coherence}")
    private boolean throughCoherence;

    public String switchmech(boolean enable) {
        this.throughCoherence = enable;
        return "COHERENCE IS: " + (enable ? "ON" : "OFF");
    }

    public boolean isEnabled() {
        return this.throughCoherence;
    }

}
