package com.familyfood.service;

import com.familyfood.dto.MenuResponse;
import com.familyfood.dto.SaveMenuRequest;

import java.time.LocalDate;

public interface MenuService {

    /**
     * 获取指定日期的菜单
     */
    MenuResponse getMenuByDate(Long familyId, LocalDate date);

    /**
     * 保存/更新每日菜单
     */
    void saveMenu(Long familyId, Long userId, SaveMenuRequest request);

    /**
     * 从菜单中移除菜品
     */
    void removeDishFromMenu(Long familyId, LocalDate date, Long dishId);
}
