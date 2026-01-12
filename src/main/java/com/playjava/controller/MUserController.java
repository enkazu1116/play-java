package com.playjava.controller;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.playjava.service.impl.MUserServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.playjava.entity.MUser;
import com.playjava.context.UserContext;

@RestController
@RequestMapping("/api/v1/users")
public class MUserController {

    private final Logger log = LoggerFactory.getLogger(MUserController.class);

    @Autowired
    private MUserServiceImpl mUserService;

    // ユーザー作成
    @PostMapping("/createUser")
    public void createUser(@RequestBody MUser user) {
        log.info("createUser: user={}", user);
        
        try {
            // ユーザー作成処理（内部でUserContextが設定される）
            mUserService.createUserImpl(user);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }
}
