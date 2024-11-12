package com.example.demo.controller;

import com.example.demo.logger.AsyncLogger;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    Logger log = new AsyncLogger(DemoController.class);

    @GetMapping("/demo")
    public String greeting() {
        log.info("request arrived");
        log.atInfo()
                .setMessage("data 1 {}")
                .addArgument(() -> maskData())
                .log();
        log.atInfo()
                .addArgument(() -> maskData())
                .log("data 1.1 {}");
        log.atInfo()
                .setMessage("data 2 {} {}")
                .addArgument(this::maskData)
                .addArgument(this::maskData)
                .log();
        log.atInfo().log("some message");
        return "demo";
    }

    private String maskData() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "long method";
    }
}