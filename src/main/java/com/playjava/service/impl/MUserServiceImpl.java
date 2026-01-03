package com.playjava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.playjava.mapper.MUserMapper;
import com.playjava.entity.MUser;
import com.playjava.service.adapter.MUserService;

import java.util.List;

/**
 * ユーザーCRUD処理
 * @author testuser
 * @version 1.0
 * @since 1.0
 * @apiNote 簡単なCRUD処理で基本理解
 */
@Service
@RequiredArgsConstructor
public class MUserServiceImpl extends ServiceImpl<MUserMapper, MUser> implements MUserService {
    
    private final MUserMapper mUserMapper;

    // テストユーザー作成
    public void createUser() {
        MUser user = new MUser();
        
        //　ユーザー設定
        user.setLoginId("customer_test");
        user.setUserName("customer_test");
        user.setEmail("customer_test@example.com");
        user.setPasswordHash("customer_test_password");
        user.setRole("customer_test");
        user.setStatus("active");
        
        // 登録
        this.save(user);
    }

    // ユーザー取得
    public MUser getMUser(Long userId) {
        return getById(userId);
    }

    // ユーザー更新
    public void updateMUser(Long userId) {
        MUser user = getById(userId);
        user.setUserName("admin");

        // 更新
        updateById(user);
    }

    // ログインIDとメールアドレスでユーザー検索
    public List<MUser> findByLoginIdAndEmail(String loginId, String email) {
        return mUserMapper.findByLoginIdAndEmail(loginId, email);
    }

    // ログインIDでユーザー検索
    public List<MUser> getMUserByLoginId(String loginId) {
         return mUserMapper.getMUserByLoginId(loginId);
    }
}
