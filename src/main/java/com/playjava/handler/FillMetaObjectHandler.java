package com.playjava.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FillMetaObjectHandler implements MetaObjectHandler{
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 新規登録時の自動設定
        log.info("新規登録 メタオブジェクト設定開始");
        this.strictInsertFill(metaObject, "createdAt", OffsetDateTime.class, OffsetDateTime.now());
        this.strictInsertFill(metaObject, "createdBy", Long.class, 123456L);
        this.setFieldValByName("updatedAt", OffsetDateTime.now(), metaObject);
        this.setFieldValByName("updatedBy", 123456L, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新時の自動設定
        log.info("更新 メタオブジェクト設定開始");
        this.strictUpdateFill(metaObject, "updatedAt", OffsetDateTime.class, OffsetDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, 123456L);
    }
}
