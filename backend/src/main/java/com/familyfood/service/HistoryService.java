package com.familyfood.service;

import com.familyfood.dto.MonthStatsResponse;

public interface HistoryService {

    /**
     * 获取月度统计
     */
    MonthStatsResponse getMonthStats(Long familyId, int year, int month);
}
