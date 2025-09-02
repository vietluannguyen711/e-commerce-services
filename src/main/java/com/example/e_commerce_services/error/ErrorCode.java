package com.example.e_commerce_services.error;

public final class ErrorCode {

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String DUPLICATE_EMAIL = "DUPLICATE_EMAIL";
    public static final String UNAUTHENTICATED = "UNAUTHENTICATED";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String SKU_NOT_FOUND = "SKU_NOT_FOUND";
    public static final String OUT_OF_STOCK = "OUT_OF_STOCK";
    public static final String PRICE_CHANGED = "PRICE_CHANGED";
    public static final String CART_CONFLICT = "CART_CONFLICT";
    public static final String RATE_LIMITED = "RATE_LIMITED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCode() {
    }
}
