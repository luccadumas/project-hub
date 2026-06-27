package com.luccadumas.projecthub.domain.risk;

import com.luccadumas.projecthub.domain.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRiskClassifierTest {

    private DefaultRiskClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new DefaultRiskClassifier();
    }

    @Test
    @DisplayName("Should classify low risk when budget and duration are within limits")
    void shouldClassifyLowRisk() {
        RiskLevel risk = classifier.classify(
                new BigDecimal("80000"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 1)
        );

        assertThat(risk).isEqualTo(RiskLevel.BAIXO);
    }

    @Test
    @DisplayName("Should classify medium risk when budget is between thresholds")
    void shouldClassifyMediumRiskByBudget() {
        RiskLevel risk = classifier.classify(
                new BigDecimal("250000"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 1)
        );

        assertThat(risk).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    @DisplayName("Should classify medium risk when duration is between 3 and 6 months")
    void shouldClassifyMediumRiskByDuration() {
        RiskLevel risk = classifier.classify(
                new BigDecimal("50000"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 1)
        );

        assertThat(risk).isEqualTo(RiskLevel.MEDIO);
    }

    @ParameterizedTest
    @CsvSource({
            "600000, 2025-01-01, 2025-03-01",
            "50000, 2025-01-01, 2025-10-01"
    })
    @DisplayName("Should classify high risk when budget or duration exceeds upper limits")
    void shouldClassifyHighRisk(String budget, String start, String end) {
        RiskLevel risk = classifier.classify(
                new BigDecimal(budget),
                LocalDate.parse(start),
                LocalDate.parse(end)
        );

        assertThat(risk).isEqualTo(RiskLevel.ALTO);
    }
}
