package com.playjava.frameworks.context;

import com.playjava.enterprise.valueobject.SystemUser;
import lombok.extern.slf4j.Slf4j;

/**
 * 現在のリクエストコンテキストでユーザーIDを管理するクラス
 * ThreadLocalを使用してスレッドごとに独立したユーザーIDを保持
 */
@Slf4j
public class UserContext {
    
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    
    /**
     * 現在のユーザーIDを設定
     * @param userId 設定するユーザーID
     */
    public static void setCurrentUserId(String userId) {
        log.debug("UserContext: ユーザーID設定 - {}", userId);
        currentUserId.set(userId);
    }
    
    /**
     * 現在のユーザーIDを取得
     * 設定されていない場合はANONYMOUSを返す
     * @return 現在のユーザーID
     */
    public static String getCurrentUserId() {
        String userId = currentUserId.get();
        if (userId == null) {
            log.debug("UserContext: ユーザーID未設定のため、ANONYMOUSを返却");
            return SystemUser.ANONYMOUS;
        }
        return userId;
    }
    
    /**
     * 現在のユーザーIDをクリア
     * リクエスト処理完了後に必ず呼び出すこと
     */
    public static void clear() {
        log.debug("UserContext: ユーザーIDクリア");
        currentUserId.remove();
    }

    /**
     * システムユーザーとして処理を実行
     * @param userId システムユーザーID (SYSTEM, ANONYMOUS, BOOTSTRAP)
     * @param runnable 実行する処理
     */
    public static void runAsSystemUser(String userId, Runnable runnable) {
        String previousUserId = currentUserId.get();
        try {
            setCurrentUserId(userId);
            runnable.run();
        } finally {
            if (previousUserId != null) {
                setCurrentUserId(previousUserId);
            } else {
                clear();
            }
        }
    }
}
