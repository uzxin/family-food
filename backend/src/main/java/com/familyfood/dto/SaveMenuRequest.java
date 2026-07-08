package com.familyfood.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaveMenuRequest {
    private LocalDate date;
    private List<Long> dishIds;
}
