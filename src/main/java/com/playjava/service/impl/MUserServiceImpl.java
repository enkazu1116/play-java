package com.playjava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.playjava.mapper.MUserMapper;
import com.playjava.entity.MUser;
import com.playjava.service.adapter.MUserService;
import com.playjava.handler.UuidFactory;
import com.playjava.context.UserContext;
import com.playjava.valueobject.SystemUser;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MUserServiceImpl extends ServiceImpl<MUserMapper, MUser> implements MUserService {
    
    private final MUserMapper mUserMapper;

    /**
     * ユーザー作成処理
     * @param user UIから@RequestBodyで受け取ったユーザー情報
     * @return 作成成功の場合true
     */
    public boolean createUserImpl(MUser user) {
        // 初期登録として実行（BOOTSTRAPユーザーとして処理）
        UserContext.setCurrentUserId(SystemUser.BOOTSTRAP);
        
        try {
            // UUIDv7を生成してユーザーIDを設定
            UUID userId = UuidFactory.newUuid();
            user.setUserId(userId.toString());

            user.setDeleteFlag(false);

            // ユーザーを保存
            boolean result = this.save(user);
            
            // 保存成功後、作成したユーザーIDをコンテキストに設定
            if (result) {
                UserContext.setCurrentUserId(user.getUserId());
            }
            
            return result;
        } catch (Exception e) {
            // エラー時はコンテキストをクリア
            UserContext.clear();
            throw e;
        }
    }
}
