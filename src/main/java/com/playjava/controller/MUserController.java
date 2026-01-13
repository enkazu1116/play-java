package com.playjava.controller;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.playjava.service.impl.MUserServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;

import com.playjava.entity.MUser;
import com.playjava.context.UserContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;


@RestController
@RequestMapping("/api/v1/users")
public class MUserController {

    private final Logger log = LoggerFactory.getLogger(MUserController.class);

    @Autowired
    private MUserServiceImpl mUserService;

    // ユーザー作成
    @PostMapping("/createUser")
    public void createUser(@Valid @RequestBody MUser user) {
        log.info("createUser: user={}", user);
        
        try {
            // ユーザー作成処理（内部でUserContextが設定される）
            mUserService.createUserImpl(user);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // ユーザー更新
    @PutMapping("/updateUser")
    public void updateUser(@Valid @RequestBody MUser user) {
        log.info("updateUser: user={}", user);
        
        try {
            // ユーザーIDをコンテキストに設定（更新者として記録）
            UserContext.setCurrentUserId(user.getUserId());
            
            // ユーザー更新処理
            mUserService.updateUserImpl(user);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // ユーザー論理削除
    @DeleteMapping("/deleteUser/{userId}")
    public void deleteUser(@PathVariable String userId) {
        log.info("deleteUser: userId={}", userId);
        
        try {
            // 削除者をコンテキストに設定
            UserContext.setCurrentUserId(userId);
            
            // ユーザー論理削除処理
            mUserService.deleteUserImpl(userId);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // ユーザー検索（ページング・ソート対応）
    @GetMapping("/searchUser")
    public IPage<MUser> searchUser(
            @RequestParam(required = false) String userName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        log.info("searchUser: userName={}, pageNum={}, pageSize={}, sortBy={}, sortOrder={}", 
            userName, pageNum, pageSize, sortBy, sortOrder);
        
        return mUserService.searchUserImpl(userName, pageNum, pageSize, sortBy, sortOrder);
    }

    // ユーザー検索（シンプル版）
    @PostMapping("/searchUserSimple")
    public List<MUser> searchUserSimple(@RequestBody MUser user) {
        log.info("searchUserSimple: user={}", user);
        
        return mUserService.searchUserImpl(user);
    }

    // ユーザー名重複チェック
    @GetMapping("/exists/{userName}")
    public boolean existsByUserName(
            @PathVariable String userName,
            @RequestParam(required = false) String excludeUserId) {
        log.info("existsByUserName: userName={}, excludeUserId={}", userName, excludeUserId);
        
        return mUserService.existsByUserName(userName, excludeUserId);
    }
}
