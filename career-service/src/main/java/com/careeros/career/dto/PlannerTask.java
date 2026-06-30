package com.careeros.career.dto;

import com.careeros.career.entity.PlannerTaskStatus;
import lombok.Data;

@Data
public class PlannerTask {
    private String id;
    private String title;
    private PlannerTaskStatus status = PlannerTaskStatus.PENDING;
}
