package com.example.e_commerce_services.exception;

public class OutOfStockException extends RuntimeException {

    private final String sku;
    private final Integer maxQty;

    public OutOfStockException(String sku, Integer maxQty, String message) {
        super(message);
        this.sku = sku;
        this.maxQty = maxQty;
    }

    public String getSku() {
        return sku;
    }

    public Integer getMaxQty() {
        return maxQty;
    }
}
