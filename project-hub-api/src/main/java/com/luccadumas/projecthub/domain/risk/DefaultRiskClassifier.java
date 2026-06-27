package com.luccadumas.projecthub.domain.risk;

import com.luccadumas.projecthub.domain.enums.RiskLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class DefaultRiskClassifier implements RiskClassificationStrategy {

    @Override
    public RiskLevel classify(BigDecimal budget, LocalDate startDate, LocalDate expectedEndDate) {
        long durationMonths = ChronoUnit.MONTHS.between(startDate, expectedEndDate);

        if (budget.compareTo(RiskThresholds.MEDIUM_BUDGET_MAX) > 0
                || durationMonths > RiskThresholds.MEDIUM_DURATION_MONTHS) {
            return RiskLevel.ALTO;
        }

        boolean mediumBudget = budget.compareTo(RiskThresholds.LOW_BUDGET_MAX) > 0
                && budget.compareTo(RiskThresholds.MEDIUM_BUDGET_MAX) <= 0;
        boolean mediumDuration = durationMonths > RiskThresholds.LOW_DURATION_MONTHS
                && durationMonths <= RiskThresholds.MEDIUM_DURATION_MONTHS;

        if (mediumBudget || mediumDuration) {
            return RiskLevel.MEDIO;
        }

        return RiskLevel.BAIXO;
    }
}
