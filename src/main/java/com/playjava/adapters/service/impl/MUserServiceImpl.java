package com.playjava.adapters.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.playjava.frameworks.mapper.MUserMapper;
import com.playjava.enterprise.entity.MUser;
import com.playjava.adapters.service.contract.MUserService;
import com.playjava.adapters.handler.UuidFactory;
import com.playjava.frameworks.context.UserContext;
import com.playjava.enterprise.valueobject.SystemUser;
import com.playjava.enterprise.valueobject.UserRole;

import java.util.UUID;
import java.util.List;
import java.time.OffsetDateTime;

import io.vavr.control.Option;

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

            // ロールを一般ユーザーに設定
            user.setRole(UserRole.GENERAL_USER);

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

    /**
     * ユーザー更新処理
     * @param user UIから@RequestBodyで受け取ったユーザー情報
     * @return 更新成功の場合true
     */
    public boolean updateUserImpl(MUser user) {
        try {
            // 更新者をコンテキストから取得
            String updateUser = UserContext.getCurrentUserId();

            // 更新内容: ユーザー名、パスワード
            user.setUpdateUser(updateUser);
            user.setUpdateDate(OffsetDateTime.now());

            // 更新処理
            boolean result = this.updateById(user);

            return result;
        } catch (Exception e) {
            // エラー時はコンテキストをクリア
            UserContext.clear();
            throw e;
        }
    }

    /**
     * ユーザー論理削除処理
     * @param userId 削除対象のユーザーID
     * @return 削除成功の場合true
     */
    public boolean deleteUserImpl(String userId) {
        try {
            // MyBatis Plusの論理削除機能を使用
            // removeByIdを使うと、@TableLogicの設定に従って自動的にdeleteFlag=trueに更新される
            boolean result = this.removeById(userId);
            
            return result;
        } catch (Exception e) {
            // エラー時はコンテキストをクリア
            UserContext.clear();
            throw e;
        }
    }

    /**
     * ユーザー検索処理（ページング・ソート対応）
     * @param userName 検索するユーザー名（部分一致）
     * @param pageNum ページ番号（1から開始）
     * @param pageSize ページサイズ
     * @param sortBy ソート対象カラム
     * @param sortOrder ソート順（asc/desc）
     * @return ページング情報を含む検索結果
     */
    public IPage<MUser> searchUserImpl(String userName, int pageNum, int pageSize, String sortBy, String sortOrder) {
        // ページング設定
        Page<MUser> page = new Page<>(pageNum, pageSize);
        
        // LambdaQueryWrapperで検索条件を組み立て
        LambdaQueryWrapper<MUser> wrapper = new LambdaQueryWrapper<>();
        
        // ユーザー名が指定されている場合、LIKE検索（部分一致）
        if (userName != null && !userName.isEmpty()) {
            wrapper.like(MUser::getUserName, userName);
        }
        
        // ソート設定
        if ("asc".equalsIgnoreCase(sortOrder)) {
            // 昇順
            switch (sortBy) {
                case "userName":
                    wrapper.orderByAsc(MUser::getUserName);
                    break;
                case "role":
                    wrapper.orderByAsc(MUser::getRole);
                    break;
                case "updateDate":
                    wrapper.orderByAsc(MUser::getUpdateDate);
                    break;
                case "createDate":
                default:
                    wrapper.orderByAsc(MUser::getCreateDate);
                    break;
            }
        } else {
            // 降順
            switch (sortBy) {
                case "userName":
                    wrapper.orderByDesc(MUser::getUserName);
                    break;
                case "role":
                    wrapper.orderByDesc(MUser::getRole);
                    break;
                case "updateDate":
                    wrapper.orderByDesc(MUser::getUpdateDate);
                    break;
                case "createDate":
                default:
                    wrapper.orderByDesc(MUser::getCreateDate);
                    break;
            }
        }
        
        return this.page(page, wrapper);
    }

    /**
     * ユーザー検索処理（シンプル版）
     * @param user 検索条件（ユーザー名による部分一致検索）
     * @return 検索結果
     */
    public List<MUser> searchUserImpl(MUser user) {
        // LambdaQueryWrapperで検索条件を組み立て
        LambdaQueryWrapper<MUser> wrapper = new LambdaQueryWrapper<>();
        
        // ユーザー名が指定されている場合、LIKE検索（部分一致）
        if (user.getUserName() != null && !user.getUserName().isEmpty()) {
            wrapper.like(MUser::getUserName, user.getUserName());
        }
          
        return this.list(wrapper);
    }

    /**
     * ユーザー名の存在チェック
     * @param userName チェックするユーザー名
     * @param excludeUserId 除外するユーザーID（更新時に自分自身を除外）
     * @return 存在する場合true
     */
    public boolean existsByUserName(String userName, String excludeUserId) {
        LambdaQueryWrapper<MUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MUser::getUserName, userName);
        
        // 業務ロジック: 更新時は自分自身を除外（Option使用）
        Option.of(excludeUserId)
            .filter(id -> !id.isEmpty())
            .forEach(id -> wrapper.ne(MUser::getUserId, id));
        
        // MyBatis呼び出し - 副作用あり（一括処理）
        return this.count(wrapper) > 0;
    }

    /**
     * ユーザー名の存在チェック（作成時用）
     * @param userName チェックするユーザー名
     * @return 存在する場合true
     */
    public boolean existsByUserName(String userName) {
        return existsByUserName(userName, null);
    }
}
