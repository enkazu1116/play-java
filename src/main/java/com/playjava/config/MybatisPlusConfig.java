package com.playjava.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 設定クラス
 * 
 * 注意：v3.5.9以降、PaginationInnerInterceptorを使用するには
 * mybatis-plus-jsqlparser依存関係が必要です。
 * 
 * 参考: https://baomidou.com/ja/plugins/pagination/
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis Plusインターセプター（ページング機能を含む）
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // ページネーションプラグインを追加
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        // 単一ページの最大件数制限（デフォルト：制限なし）
        paginationInnerInterceptor.setMaxLimit(1000L);
        // オーバーフロー時の処理（デフォルト：false）
        paginationInnerInterceptor.setOverflow(false);
        
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        return interceptor;
    }
}
