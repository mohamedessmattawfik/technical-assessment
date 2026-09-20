package com.mal.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public enum Currency {
    AED(2),
    BHD(3);


    private final int scale;


    Currency(int scale) {
        this.scale = scale;
    }

    public int getScale() {
        return scale;
    }

    public BigDecimal round(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        return amount.setScale(this.scale, RoundingMode.HALF_UP);
    }
}
