package com.jzyz.gtreviewassistant.common;

import java.util.Locale;

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

    public static String searchKey(String value) {
        return clean(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s\\u3000/／\\\\\\-－—_|:：;；,，.。()（）]+", "");
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
