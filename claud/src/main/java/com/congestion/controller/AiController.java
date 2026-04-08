package com.congestion.controller;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.congestion.dto.CongestionContext;
import com.congestion.service.ClaudeService;
import com.congestion.service.PublicApiService;

import lombok.RequiredArgsConstructor;

/**
 * Claude AI 인사이트 REST 컨트롤러.
 *
 * 프론트엔드의 getAiInsight(body) 호출을 받아 ClaudeService로 위임한다.
 * 요청 바디에 stationName이 포함되면 실시간 혼잡도·날씨·요일을 자동으로 조합해
 * Claude에게 전달함으로써 더 정확한 자연어 응답을 생성한다.
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final ClaudeService claudeService;
    private final PublicApiService publicApiService;

    /**
     * AI 인사이트 생성.
     * POST /ai/insight
     *
     * 요청 바디 (모두 선택):
     * - userQuery  : 사용자 자연어 질문 (예: "지금 2호선 강남역 어때?")
     * - stationName: 역명 (예: "강남") — 있으면 실시간 혼잡도 자동 조회
     * - line       : 호선 (예: "2호선")
     * - direction  : 방향 (예: "내선")
     *
     * 응답: plain text (AI 생성 한국어 문장)
     */
    @PostMapping("/insight")
    public String getInsight(@RequestBody Map<String, String> body) {
        String userQuery   = body.get("userQuery");
        String stationName = body.get("stationName");
        String line        = body.get("line");
        String direction   = body.get("direction");

        // 현재 요일을 한국어로 변환 (예: "화요일")
        DayOfWeek dow = LocalDateTime.now().getDayOfWeek();
        String dayOfWeek = dow.getDisplayName(TextStyle.FULL, Locale.KOREAN);

        CongestionContext.CongestionContextBuilder builder = CongestionContext.builder()
                .userQuery(userQuery)
                .stationName(stationName)
                .line(line)
                .direction(direction)
                .currentTime(LocalDateTime.now())
                .dayOfWeek(dayOfWeek)
                .weather(publicApiService.fetchWeather()); // 기상청 API (현재 "맑음" 기본값)

        // 역명이 있으면 공공 API로 실시간 혼잡도 추정해 컨텍스트에 추가
        if (stationName != null && !stationName.isBlank()) {
            int rate = publicApiService.estimateCongestion(stationName);
            builder.currentRate(rate);
        }

        return claudeService.getInsight(builder.build());
    }
}
