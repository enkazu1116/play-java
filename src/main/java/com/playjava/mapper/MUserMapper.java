package com.playjava.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.playjava.entity.MUser;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

/**
 * ユーザーマッパー
 * @author testuser
 * @version 1.0
 * @since 1.0
 * @apiNote 
 * アノテーションを利用したSQLの作成
 */
@Mapper
public interface MUserMapper extends BaseMapper<MUser>{

    // ログインID検索
    @Select("SELECT * FROM m_user WHERE login_id = #{loginId}")
    List<MUser> getMUserByLoginId(@Param("loginId") String loginId);

    // 動的クエリの作成
    @Select("""
            <script>
            SELECT * FROM m_user
            WHERE 1=1
            <if test="loginId != null and loginId != ''">
              AND login_id = #{loginId}
            </if>
            <if test="email != null and email != ''">
              AND email LIKE CONCAT('%', #{email}, '%')
            </if>
            ORDER BY created_at DESC
            </script>
    """)
    List<MUser> findByLoginIdAndEmail(@Param("loginId") String loginId, @Param("email") String email);
}
