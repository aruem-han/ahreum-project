package com.congestion.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.congestion.dto.CongestionContext;


@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final RestTemplate restTemplate;
    
    @Value("${app.anthropic.api-key}"  )
    private String apiKey;

    @Value("${app.anthropic.model}")
    private String model;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";  

    public String getInsight(CongestionContext ctx){
        String query = ctx.getUserQuery() != null ? ctx.getUserQuery() : "";

        if (query.contains("언제") || query.contains("시간")) {
                return "19시 이후에 출발하면 혼잡도가 절반 이하로 줄어들 거예요. " +
               "지금보다 30분만 기다리셔도 훨씬 여유롭게 탈 수 있어요.";
        }

        if(query.contains("날씨") || query.contains("비")){
              return "오늘 비 때문에 평소보다 약 15% 더 붐비고 있어요. " +
               "우산 챙기시고 가급적 첫 번째나 마지막 칸을 이용해보세요.";
        }
        if (query.contains("추천") || query.contains("노선")) {
            return "지금 시간대엔 9호선보다 2호선이 상대적으로 여유로워요. " +
                "환승이 가능하다면 2호선을 추천드려요.";
        }
        // 기본응답
        if(ctx.getStationName() != null){
             return String.format(
            "지금 %s역 %s은 %s 상태예요. " +
            "오늘 %s 기준 평균보다 %s 혼잡도를 보이고 있어요. " +
            "가능하면 양쪽 끝 칸을 이용하시면 조금 더 여유로울 거예요.",
            ctx.getStationName(),
            ctx.getLine(),
            getCongestionLabel(ctx.getCurrentRate()),
            ctx.getDayOfWeek(),
            ctx.getCurrentRate() > 70 ? "높은" : "비슷한"
            
            );
     }  
     return "서울 지하철 현재 전반적인 혼잡 상황은 보통이에요. " +
            "출발 시간대를 조금 조정하거나, 환승 가능한 노선이 있다면 그쪽을 이용하시면 좀 더 편안하게 이동하실 수 있을 거예요.";
    }
 
    private String getCongestionLabel(int rate) {
        if(rate >= 80) return "매우 혼잡";
        if(rate >= 60) return "혼잡";
        if(rate >= 50) return "보통"; 
        return "여유";
    }
 
    private String buildSystemPrompt(){
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
 
    private String buildUserPrompt(CongestionContext ctx){
        if(ctx.getStationName() == null){
                return "서울 지하철 현재 전반적인 혼잡 상황을 친근하게 요약해줘.\n질문: " + ctx.getUserQuery();
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

    private String formatTrend(List<Integer> trend) {
        if (trend == null || trend.isEmpty()) return "데이터 없음";
        return IntStream.range(0, trend.size())
            .mapToObj(i -> (18 + i / 2) + "시" + (i % 2 == 0 ? "" : "30분") + ":" + trend.get(i) + "%")
            .collect(Collectors.joining(" → "));
    }
    private String formatHistory(Map<String, Integer> history) {
        if (history == null || history.isEmpty()) return "데이터 없음";
        return history.entrySet().stream()
            .map(e -> e.getKey() + "평균: " + e.getValue() + "%")
            .collect(Collectors.joining(", "));
    }
}