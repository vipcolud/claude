package com.fundanalysis.entity;

public enum FundType {
    STOCK("股票型"),
    BOND("债券型"),
    MIXED("混合型");

    private final String displayName;

    FundType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
