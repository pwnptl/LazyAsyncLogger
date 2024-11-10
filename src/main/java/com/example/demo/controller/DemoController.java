package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DemoController {


    @GetMapping("/demo")
    public String greeting() {
        log.atInfo().setMessage("data 2 {}")
                .addArgument(() -> maskData())
                .log();
        return "demo";
    }

    private String maskData() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "long method";
    }
}