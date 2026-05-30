package com.jzyz.gtreviewassistant.common;

import java.util.Locale;

public final class Side {
    public static final String PDF = "PDF";
    public static final String EXCEL = "EXCEL";

    private Side() {
    }

    public static String normalize(String side) {
        if (side == null || side.isBlank()) {
            throw new IllegalArgumentException("side 不能为空，必须是 PDF 或 EXCEL");
        }
        String normalized = side.trim().toUpperCase(Locale.ROOT);
        if (!PDF.equals(normalized) && !EXCEL.equals(normalized)) {
            throw new IllegalArgumentException("side 只能是 PDF 或 EXCEL");
        }
        return normalized;
    }
}