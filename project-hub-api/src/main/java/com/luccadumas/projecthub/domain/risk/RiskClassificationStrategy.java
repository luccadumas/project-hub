package com.luccadumas.projecthub.domain.risk;

import com.luccadumas.projecthub.domain.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public interface RiskClassificationStrategy {

    RiskLevel classify(BigDecimal budget, LocalDate startDate, LocalDate expectedEndDate);
}
