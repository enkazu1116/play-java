package com.playjava.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * グローバル例外ハンドラー
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 在庫不足例外
     * 在庫不足の商品情報を含む
     */
    @Getter
    public static class StockInsufficientException extends RuntimeException {
        private final List<StockInsufficientItem> insufficientItems;
        
        public StockInsufficientException(String message, List<StockInsufficientItem> insufficientItems) {
            super(message);
            this.insufficientItems = insufficientItems;
        }
        
        @Getter
        public static class StockInsufficientItem {
            private final String productId;
            private final Integer availableQuantity;
            private final Integer requestedQuantity;
            
            public StockInsufficientItem(String productId, Integer availableQuantity, Integer requestedQuantity) {
                this.productId = productId;
                this.availableQuantity = availableQuantity;
                this.requestedQuantity = requestedQuantity;
            }
        }
    }

    /**
     * バリデーションエラーのハンドリング
     * @param ex MethodArgumentNotValidException
     * @return エラーレスポンス
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        // 各フィールドのバリデーションエラーを収集
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "バリデーションエラー");
        response.put("errors", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 在庫不足例外のハンドリング
     * @param ex StockInsufficientException
     * @return エラーレスポンス（在庫不足商品情報を含む）
     */
    @ExceptionHandler(StockInsufficientException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsufficientException(StockInsufficientException ex) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", ex.getMessage());
        response.put("insufficientItems", ex.getInsufficientItems().stream()
            .map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("availableQuantity", item.getAvailableQuantity());
                itemMap.put("requestedQuantity", item.getRequestedQuantity());
                return itemMap;
            })
            .collect(Collectors.toList()));
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
