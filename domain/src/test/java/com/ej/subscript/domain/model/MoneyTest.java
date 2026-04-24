package com.ej.subscript.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import com.ej.subscript.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldCreateMoneySuccessfully() {
        Money money = new Money(new BigDecimal("10000"), "COP");

        assertThat(money.amount()).isEqualByComparingTo("10000");
        assertThat(money.currency()).isEqualTo("COP");
    }

    @Test
    void shouldCreateCopWithFactory() {
        Money money = Money.cop(new BigDecimal("5000"));

        assertThat(money.currency()).isEqualTo("COP");
        assertThat(money.amount()).isEqualByComparingTo("5000");
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        assertThatThrownBy(() -> new Money(null, "COP"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1"), "COP"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        assertThatThrownBy(() -> new Money(new BigDecimal("1000"), null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenCurrencyIsBlank() {
        assertThatThrownBy(() -> new Money(new BigDecimal("1000"), "  "))
                .isInstanceOf(BusinessException.class);
    }
}
