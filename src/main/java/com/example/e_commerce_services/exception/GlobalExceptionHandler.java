package com.example.e_commerce_services.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.e_commerce_services.error.ApiErrorResponse;
import static com.example.e_commerce_services.error.ErrorCode.DUPLICATE_EMAIL;
import static com.example.e_commerce_services.error.ErrorCode.INTERNAL_ERROR;
import static com.example.e_commerce_services.error.ErrorCode.NOT_FOUND;
import static com.example.e_commerce_services.error.ErrorCode.OUT_OF_STOCK;
import static com.example.e_commerce_services.error.ErrorCode.PRICE_CHANGED;
import static com.example.e_commerce_services.error.ErrorCode.RATE_LIMITED;
import static com.example.e_commerce_services.error.ErrorCode.SKU_NOT_FOUND;
import static com.example.e_commerce_services.error.ErrorCode.UNAUTHENTICATED;
import static com.example.e_commerce_services.error.ErrorCode.UNAUTHORIZED;
import static com.example.e_commerce_services.error.ErrorCode.VALIDATION_ERROR;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 400 - VALIDATION_ERROR (Bean Validation @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        return build(VALIDATION_ERROR, "Dữ liệu không hợp lệ", details, req, HttpStatus.BAD_REQUEST);
    }

    // 404 - NOT_FOUND
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(NOT_FOUND, ex.getMessage(), null, req, HttpStatus.NOT_FOUND);
    }

    // 404 - SKU_NOT_FOUND
    @ExceptionHandler(SkuNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSkuNotFound(SkuNotFoundException ex, HttpServletRequest req) {
        return build(SKU_NOT_FOUND, ex.getMessage(), null, req, HttpStatus.NOT_FOUND);
    }

    // 409 - OUT_OF_STOCK
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ApiErrorResponse> handleOutOfStock(OutOfStockException ex, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        details.put("sku", ex.getSku());
        if (ex.getMaxQty() != null) {
            details.put("maxQty", ex.getMaxQty());
        }
        return build(OUT_OF_STOCK, ex.getMessage(), details, req, HttpStatus.CONFLICT);
    }

    // 409 - PRICE_CHANGED
    @ExceptionHandler(PriceChangedException.class)
    public ResponseEntity<ApiErrorResponse> handlePriceChanged(PriceChangedException ex, HttpServletRequest req) {
        Map<String, Object> details = Map.of(
                "oldPrice", ex.getOldPrice(),
                "newPrice", ex.getNewPrice()
        );
        return build(PRICE_CHANGED, ex.getMessage(), details, req, HttpStatus.CONFLICT);
    }

    // 409 - DUPLICATE_EMAIL
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest req) {
        return build(DUPLICATE_EMAIL, ex.getMessage(), null, req, HttpStatus.CONFLICT);
    }

    // 401 - UNAUTHENTICATED
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthenticated(UnauthenticatedException ex, HttpServletRequest req) {
        return build(UNAUTHENTICATED, ex.getMessage(), null, req, HttpStatus.UNAUTHORIZED);
    }

    // 403 - UNAUTHORIZED
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return build(UNAUTHORIZED, ex.getMessage(), null, req, HttpStatus.FORBIDDEN);
    }

    // 429 - RATE_LIMITED
    @ExceptionHandler(RateLimitedException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimited(RateLimitedException ex, HttpServletRequest req) {
        return build(RATE_LIMITED, ex.getMessage(), null, req, HttpStatus.TOO_MANY_REQUESTS);
    }

    // 400 - fallback cho IllegalArgumentException (thường do validate thủ công)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        String msg = ex.getMessage() == null ? "Dữ liệu không hợp lệ" : ex.getMessage();
        return build(VALIDATION_ERROR, msg, null, req, HttpStatus.BAD_REQUEST);
    }

    // 500 - INTERNAL_ERROR (bắt mọi thứ còn lại)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        // TODO: log requestId + stacktrace ở đây (logger)
        return build(INTERNAL_ERROR, "Đã có lỗi xảy ra. Vui lòng thử lại.", null, req, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiErrorResponse> build(String code, String message, Map<String, Object> details,
            HttpServletRequest req, HttpStatus status) {
        String path = req.getRequestURI();
        ApiErrorResponse body = ApiErrorResponse.of(code, message, details, path);
        return ResponseEntity.status(status).body(body);
    }
}
