package com.example.e_commerce_services.exception;

public class PriceChangedException extends RuntimeException {

    private final Integer oldPrice;
    private final Integer newPrice;

    public PriceChangedException(Integer oldPrice, Integer newPrice, String message) {
        super(message);
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }

    public Integer getOldPrice() {
        return oldPrice;
    }

    public Integer getNewPrice() {
        return newPrice;
    }
}
