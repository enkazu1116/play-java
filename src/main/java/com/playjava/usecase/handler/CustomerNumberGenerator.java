package com.playjava.usecase.handler;

import java.util.Random;

/**
 * 顧客番号生成ユーティリティ
 */
public class CustomerNumberGenerator {
    
    private static final Random random = new Random();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    
    /**
     * 顧客番号を生成する
     * 形式: 4文字のランダム英字（大文字） + 6桁のランダム数字
     * 例: ABCD123456, WXYZ789012
     * 
     * @return 顧客番号（10文字）
     */
    public static String generate() {
        StringBuilder sb = new StringBuilder();
        
        // 先頭4文字: ランダムな英字（A-Z、大文字のみ）
        for (int i = 0; i < 4; i++) {
            sb.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        
        // 後ろ6桁: ランダムな数字（0-9）
        for (int i = 0; i < 6; i++) {
            sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        
        return sb.toString();
    }
}
