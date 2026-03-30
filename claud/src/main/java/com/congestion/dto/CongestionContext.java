
package com.congestion.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CongestionContext {

    private String userQuery;
    private Map<String, Integer> historicalAvg;
    private String StationName;
    private int Line;
    private String Direction;
    private int CurrentRate;
    private LocalDateTime CurrentTime;
    private String DayOfWeek;
    private String Weather;
    private List<Integer> RecentTrend;

    
}
