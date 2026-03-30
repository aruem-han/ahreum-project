
package com.congestion.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.congestion.dto.CongestionContext;
import com.congestion.service.ClaudeService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    private final ClaudeService claudeService;
  
  @PostMapping("/test")
    public String test(@RequestBody Map<String, String> body) {
        CongestionContext ctx = CongestionContext.builder()
            .userQuery(body.get("userQuery"))
            .build();
        return claudeService.getInsight(ctx);
    }
}
