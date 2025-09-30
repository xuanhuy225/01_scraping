package com.example.scraping;

public enum SrcapSource {
    BAOMOI(1, "BaoMoi");

    private final int code;
    private final String description;

    SrcapSource(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
