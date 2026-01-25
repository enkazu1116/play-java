package com.playjava.application.controller;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.playjava.usecase.service.impl.MUserServiceImpl;

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

import com.playjava.enterprise.entity.MUser;
import com.playjava.frameworks.context.UserContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;


@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "ユーザー管理", description = "ユーザーマスタのCRUD操作とユーザー検索API")
public class MUserController {

    private final Logger log = LoggerFactory.getLogger(MUserController.class);

    @Autowired
    private MUserServiceImpl mUserService;

    // ユーザー作成
    @Operation(
        summary = "ユーザー作成",
        description = "新規ユーザーを作成します。ユーザーIDは自動生成されます。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "作成成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/createUser")
    public void createUser(
            @Parameter(description = "作成するユーザー情報", required = true)
            @Valid @RequestBody MUser user) {
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
    @Operation(
        summary = "ユーザー更新",
        description = "既存ユーザーの情報を更新します。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
        @ApiResponse(responseCode = "404", description = "ユーザーが見つかりません")
    })
    @PutMapping("/updateUser")
    public void updateUser(
            @Parameter(description = "更新するユーザー情報", required = true)
            @Valid @RequestBody MUser user) {
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
    @Operation(
        summary = "ユーザー削除",
        description = "指定されたユーザーを論理削除します。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "削除成功"),
        @ApiResponse(responseCode = "404", description = "ユーザーが見つかりません")
    })
    @DeleteMapping("/deleteUser/{userId}")
    public void deleteUser(
            @Parameter(description = "削除するユーザーのID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String userId) {
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
    @Operation(
        summary = "ユーザー検索（ページング対応）",
        description = "ユーザー名で検索し、ページング・ソート機能を提供します。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "検索成功")
    })
    @GetMapping("/searchUser")
    public IPage<MUser> searchUser(
            @Parameter(description = "ユーザー名（部分一致）", example = "yamada")
            @RequestParam(required = false) String userName,
            @Parameter(description = "ページ番号（1から開始）", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "1ページあたりの件数", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "ソート対象フィールド", example = "createDate")
            @RequestParam(defaultValue = "createDate") String sortBy,
            @Parameter(description = "ソート順序（asc/desc）", example = "desc")
            @RequestParam(defaultValue = "desc") String sortOrder) {
        log.info("searchUser: userName={}, pageNum={}, pageSize={}, sortBy={}, sortOrder={}", 
            userName, pageNum, pageSize, sortBy, sortOrder);
        
        return mUserService.searchUserImpl(userName, pageNum, pageSize, sortBy, sortOrder);
    }

    // ユーザー検索（シンプル版）
    @Operation(
        summary = "ユーザー検索（シンプル版）",
        description = "ユーザー情報を条件にして検索します。ページング無し。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "検索成功")
    })
    @PostMapping("/searchUserSimple")
    public List<MUser> searchUserSimple(
            @Parameter(description = "検索条件となるユーザー情報", required = true)
            @RequestBody MUser user) {
        log.info("searchUserSimple: user={}", user);
        
        return mUserService.searchUserImpl(user);
    }

    // ユーザー名重複チェック
    @Operation(
        summary = "ユーザー名重複チェック",
        description = "指定されたユーザー名が既に使用されているかチェックします。"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "チェック成功（true:重複あり, false:重複なし）")
    })
    @GetMapping("/exists/{userName}")
    public boolean existsByUserName(
            @Parameter(description = "チェックするユーザー名", required = true, example = "yamada_taro")
            @PathVariable String userName,
            @Parameter(description = "除外するユーザーID（更新時に自分を除外）", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) String excludeUserId) {
        log.info("existsByUserName: userName={}, excludeUserId={}", userName, excludeUserId);
        
        return mUserService.existsByUserName(userName, excludeUserId);
    }
}
