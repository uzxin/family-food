package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.dto.MonthStatsResponse;
import com.familyfood.entity.Family;
import com.familyfood.service.FamilyService;
import com.familyfood.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "历史记录与统计")
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;
    private final FamilyService familyService;

    @Operation(summary = "月度统计")
    @GetMapping("/stats/{year}/{month}")
    public Result<MonthStatsResponse> getMonthStats(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId,
            @PathVariable int year,
            @PathVariable int month) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        return Result.ok(historyService.getMonthStats(family.getId(), year, month));
    }
}
