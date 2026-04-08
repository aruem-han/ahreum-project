package com.congestion.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.congestion.dto.CongestionContext;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI ChatGPT API를 통한 AI 혼잡도 인사이트 생성 서비스.
 *
 * 흐름:
 *  1. getInsight() → callChatGptApi() 시도
 *  2. API 호출 성공 시: ChatGPT가 생성한 자연어 문장 반환
 *  3. API 키 미설정/네트워크 오류 등 실패 시: getLocalInsight()로 폴백
 *     (키워드·혼잡도 기반 규칙 응답 — 서비스 중단 없이 동작 보장)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final RestTemplate restTemplate;

    /** application.yaml: app.openai.api-key */
    @Value("${app.openai.api-key}")
    private String apiKey;

    /** application.yaml: app.openai.model (예: gpt-4o) */
    @Value("${app.openai.model}")
    private String model;

    // Groq 사용 시: "https://api.groq.com/openai/v1/chat/completions"
    // OpenAI 사용 시: "https://api.openai.com/v1/chat/completions"
    private static final String OPENAI_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    // -------------------------------------------------------------------------
    // 공개 API
    // -------------------------------------------------------------------------

    /**
     * 사용자 질문 + 혼잡도 컨텍스트를 받아 한국어 AI 답변을 반환.
     * ChatGPT API 호출에 실패하면 로컬 규칙 응답으로 자동 폴백한다.
     *
     * @param ctx CongestionContext (userQuery 필수, 나머지 선택)
     * @return 한국어 자연어 인사이트 문장
     */
    public String getInsight(CongestionContext ctx) {
        try {
            return callChatGptApi(ctx);
        } catch (Exception e) {
            log.warn("ChatGPT API 호출 실패 — 로컬 응답으로 폴백: {}", e.getMessage());
            return getLocalInsight(ctx);
        }
    }

    // -------------------------------------------------------------------------
    // ChatGPT API 호출
    // -------------------------------------------------------------------------

    /**
     * OpenAI Chat Completions API 호출.
     *
     * 요청 구조:
     * {
     *   "model": "gpt-4o",
     *   "messages": [
     *     { "role": "system", "content": "..." },   ← buildSystemPrompt()
     *     { "role": "user",   "content": "..." }    ← buildUserPrompt()
     *   ],
     *   "max_tokens": 300,
     *   "temperature": 0.7
     * }
     *
     * 응답에서 choices[0].message.content 를 추출해 반환한다.
     */
    @SuppressWarnings("unchecked")
    private String callChatGptApi(CongestionContext ctx) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey != null ? apiKey : ""); // Authorization: Bearer {apiKey}

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 300);
        requestBody.put("temperature", 0.7);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", buildSystemPrompt()),
                Map.of("role", "user",   "content", buildUserPrompt(ctx))
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_API_URL, HttpMethod.POST, request, Map.class
        );

        // 응답 파싱: choices[0].message.content
        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    // -------------------------------------------------------------------------
    // 시스템 프롬프트 / 유저 프롬프트 빌더
    // -------------------------------------------------------------------------

    /**
     * ChatGPT에게 역할과 답변 규칙을 부여하는 시스템 프롬프트.
     * - 2~4문장, 직관적 표현, 실용적 팁 1개 포함, 이모지 금지
     */
    private String buildSystemPrompt() {
        return """
                너는 서울 대중교통 혼잡도 분석 전문가야.
                사용자에게 친근하고 실용적인 한국어로 답변해.

                답변 규칙:
                - 2~4문장으로 간결하게
                - 숫자보다 "여유", "보통", "혼잡" 같은 직관적 표현 우선
                - 반드시 실용적인 팁 1가지 포함
                - 이모지 사용 금지
                """;
    }

    /**
     * 사용자 프롬프트 구성.
     * stationName이 없으면 전체 노선 일반 요약 요청,
     * 있으면 역·혼잡도·날씨·추이 등 상세 컨텍스트를 포함한 정형화된 질문을 전송한다.
     */
    private String buildUserPrompt(CongestionContext ctx) {
        if (ctx.getStationName() == null || ctx.getStationName().isBlank()) {
            return "서울 지하철 현재 전반적인 혼잡 상황을 친근하게 요약해줘.\n질문: "
                    + ctx.getUserQuery();
        }

        return """
                [역 정보]
                역명: %s / 호선: %s / 방향: %s

                [현재 상황]
                혼잡도: %d%% / 시각: %s / 요일: %s / 날씨: %s

                [최근 1시간 추이]
                %s

                [같은 요일 과거 평균]
                %s

                [사용자 질문]
                %s
                """.formatted(
                ctx.getStationName(),
                ctx.getLine(),
                ctx.getDirection(),
                ctx.getCurrentRate(),
                ctx.getCurrentTime(),
                ctx.getDayOfWeek(),
                ctx.getWeather(),
                formatTrend(ctx.getRecentTrend()),
                formatHistory(ctx.getHistoricalAvg()),
                ctx.getUserQuery()
        );
    }

    // -------------------------------------------------------------------------
    // 로컬 폴백 응답 (ChatGPT API 실패 시)
    // -------------------------------------------------------------------------

    /**
     * API 호출 없이 키워드 규칙과 혼잡도 수치만으로 답변을 생성한다.
     * 네트워크 장애 또는 API 키 미설정 상태에서도 서비스가 응답할 수 있도록 보장한다.
     */
    private String getLocalInsight(CongestionContext ctx) {
        String query = ctx.getUserQuery() != null ? ctx.getUserQuery() : "";

        // 시간대 관련 질문
        if (query.contains("언제") || query.contains("시간")) {
            return "19시 이후에 출발하면 혼잡도가 절반 이하로 줄어들 거예요. "
                    + "지금보다 30분만 기다리셔도 훨씬 여유롭게 탈 수 있어요.";
        }

        // 날씨 관련 질문
        if (query.contains("날씨") || query.contains("비")) {
            return "오늘 비 때문에 평소보다 약 15% 더 붐비고 있어요. "
                    + "우산 챙기시고 가급적 첫 번째나 마지막 칸을 이용해보세요.";
        }

        // 노선 추천 질문
        if (query.contains("추천") || query.contains("노선")) {
            return "지금 시간대엔 9호선보다 2호선이 상대적으로 여유로워요. "
                    + "환승이 가능하다면 2호선을 추천드려요.";
        }

        // 특정 역 컨텍스트가 있는 경우
        if (ctx.getStationName() != null && !ctx.getStationName().isBlank()) {
            return String.format(
                    "지금 %s역 %s은 %s 상태예요. "
                    + "오늘 %s 기준 평균보다 %s 혼잡도를 보이고 있어요. "
                    + "가능하면 양쪽 끝 칸을 이용하시면 조금 더 여유로울 거예요.",
                    ctx.getStationName(),
                    ctx.getLine() != null ? ctx.getLine() : "",
                    getCongestionLabel(ctx.getCurrentRate()),
                    ctx.getDayOfWeek() != null ? ctx.getDayOfWeek() : "오늘",
                    ctx.getCurrentRate() > 70 ? "높은" : "비슷한"
            );
        }

        // 완전 일반 응답
        return "서울 지하철 현재 전반적인 혼잡 상황은 보통이에요. "
                + "출발 시간대를 조금 조정하거나, 환승 가능한 노선이 있다면 그쪽을 이용하시면 좀 더 편안하게 이동하실 수 있을 거예요.";
    }

    // -------------------------------------------------------------------------
    // 내부 헬퍼 메서드
    // -------------------------------------------------------------------------

    /** 혼잡도 숫자(0~100)를 직관적 한국어 레이블로 변환 */
    private String getCongestionLabel(int rate) {
        if (rate >= 85) return "매우 혼잡";
        if (rate >= 65) return "혼잡";
        if (rate >= 40) return "보통";
        return "여유";
    }

    /**
     * 최근 1시간 추이 리스트를 시간 문자열로 포매팅.
     * 예: [45, 62] → "18시:45% → 18시30분:62%"
     */
    private String formatTrend(List<Integer> trend) {
        if (trend == null || trend.isEmpty()) return "데이터 없음";
        return IntStream.range(0, trend.size())
                .mapToObj(i -> (18 + i / 2) + "시"
                        + (i % 2 == 0 ? "" : "30분") + ":" + trend.get(i) + "%")
                .collect(Collectors.joining(" → "));
    }

    /**
     * 요일별 과거 평균을 문자열로 포매팅.
     * 예: {"07시": 72, "08시": 88} → "07시평균: 72%, 08시평균: 88%"
     */
    private String formatHistory(Map<String, Integer> history) {
        if (history == null || history.isEmpty()) return "데이터 없음";
        return history.entrySet().stream()
                .map(e -> e.getKey() + "평균: " + e.getValue() + "%")
                .collect(Collectors.joining(", "));
    }
}
