package com.plip.chat.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Health", description = "기동 확인")
public class TestController {

    @GetMapping("/api/test")
    @Operation(summary = "기동 확인")
    public String test() {
        log.info("log@@");
        return "test success!";
    }
}
