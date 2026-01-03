package com.playjava.controller;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.playjava.service.impl.MUserServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.playjava.entity.MUser;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class MUserController {

    private final Logger log = LoggerFactory.getLogger(MUserController.class);

    @Autowired
    private MUserServiceImpl mUserService;
    
    // ユーザー作成
    // Mybatis Plusの一般的な内容を使用
    @PostMapping("/createUser")
    public void createUser() {
        mUserService.createUser();
    }

    // ユーザーID検索
    // Mybatis Plusの一般的な内容を使用
    @GetMapping("/getUser/{userId}")
    public MUser getUser(@PathVariable Long userId) {
        log.info("getUser: userId={}", userId);
        return mUserService.getMUser(userId);
    }

    // ログインID検索
    // Mybatis PlusのアノテーションベースでカスタムSQLを作成したものを使用
    @GetMapping("/getUserByLoginId/{loginId}")
    public List<MUser> getUserByLoginId(@PathVariable String loginId) {
        log.info("getUserByLoginId: loginId={}", loginId);
        return mUserService.getMUserByLoginId(loginId);
    }

    // ログインIDとメールアドレスでユーザー検索
    // Mybatis PlusのアノテーションベースでカスタムSQLを作成したものを使用
    @GetMapping("/findByLoginIdAndEmail/{loginId}/{email}")
    public List<MUser> findByLoginIdAndEmail(@PathVariable String loginId, @PathVariable String email) {
        log.info("findByLoginIdAndEmail: loginId={}, email={}", loginId, email);
        return mUserService.findByLoginIdAndEmail(loginId, email);
    }
}
