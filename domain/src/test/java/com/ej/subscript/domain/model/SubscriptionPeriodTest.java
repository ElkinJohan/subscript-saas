package com.ej.subscript.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionPeriodTest {

    @Test
    void shouldCreatePeriodWithCorrectEndDate() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        SubscriptionPeriod period = SubscriptionPeriod.of(start, 30);

        assertThat(period.endDate()).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        LocalDate start = LocalDate.of(2026, 1, 31);
        LocalDate end = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> new SubscriptionPeriod(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void shouldRejectNullDates() {
        assertThatThrownBy(() -> new SubscriptionPeriod(null, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDetectExpiredPeriod() {
        SubscriptionPeriod expired = SubscriptionPeriod.of(LocalDate.now().minusDays(40), 30);

        assertThat(expired.isExpired()).isTrue();
    }

    @Test
    void shouldDetectActivePeriod() {
        SubscriptionPeriod active = SubscriptionPeriod.of(LocalDate.now(), 30);

        assertThat(active.isExpired()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenDaysUntilExpirationIsWithinWindow() {
        SubscriptionPeriod period = SubscriptionPeriod.of(LocalDate.now(), 5);

        assertThat(period.isDueForRenewal(7)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDaysUntilExpirationExceedsWindow() {
        SubscriptionPeriod period = SubscriptionPeriod.of(LocalDate.now(), 10);

        assertThat(period.isDueForRenewal(7)).isFalse();
    }
}
