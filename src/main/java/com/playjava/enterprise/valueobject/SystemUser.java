package com.playjava.enterprise.valueobject;

/**
 * システムユーザーを表すValue Object
 * 認証されていない状態や、システム起点の処理で使用する固定のユーザーID
 */
public class SystemUser {
    
    /**
     * システム起点の処理で使用するユーザーID
     * UUID形式: 00000000-0000-0000-0000-000000000001
     */
    public static final String SYSTEM = "00000000-0000-0000-0000-000000000001";
    
    /**
     * 未認証ユーザーの処理で使用するユーザーID
     * UUID形式: 00000000-0000-0000-0000-000000000002
     */
    public static final String ANONYMOUS = "00000000-0000-0000-0000-000000000002";
    
    /**
     * 初期登録（Bootstrap）で使用するユーザーID
     * UUID形式: 00000000-0000-0000-0000-000000000003
     */
    public static final String BOOTSTRAP = "00000000-0000-0000-0000-000000000003";
    
    private SystemUser() {}
    
    /**
     * 文字列がシステムユーザーIDかどうかを判定
     * @param userId 判定するユーザーID
     * @return システムユーザーIDの場合true
     */
    public static boolean isSystemUser(String userId) {
        return SYSTEM.equals(userId) || ANONYMOUS.equals(userId) || BOOTSTRAP.equals(userId);
    }
}
