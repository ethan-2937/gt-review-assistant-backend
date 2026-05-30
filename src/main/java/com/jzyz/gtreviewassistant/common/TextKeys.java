package com.jzyz.gtreviewassistant.common;

public final class TextKeys {
    private TextKeys() {
    }

    public static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String key(String value) {
        return clean(value)
                .replace("／", "/")
                .replace("\\", "/")
                .replaceAll("\\s*/\\s*", "/");
    }

    public static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String cleaned = clean(value);
            if (!cleaned.isEmpty()) {
                return cleaned;
            }
        }
        return "";
    }
}