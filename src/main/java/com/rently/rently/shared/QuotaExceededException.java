package com.rently.rently.shared;

public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}
