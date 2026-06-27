package com.luccadumas.projecthub.domain.risk;

import java.math.BigDecimal;

public final class RiskThresholds {

    public static final BigDecimal LOW_BUDGET_MAX = new BigDecimal("100000");
    public static final BigDecimal MEDIUM_BUDGET_MAX = new BigDecimal("500000");
    public static final long LOW_DURATION_MONTHS = 3;
    public static final long MEDIUM_DURATION_MONTHS = 6;

    private RiskThresholds() {
    }
}
