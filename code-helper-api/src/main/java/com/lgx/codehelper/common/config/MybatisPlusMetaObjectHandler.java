package com.lgx.codehelper.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lgx.codehelper.common.filter.auth.UserInfoContextHolder;
import com.lgx.codehelper.common.filter.auth.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MybatisPlus 自动填充配置
 * @author LGX_TvT <br>
 * @version 1.0 <br>
 * Create by 2022-04-04 22:55 <br>
 * @description: JyMybatisPlusMetaObjectHandler <br>
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        UserInfo userInfo = null;
        try {
            userInfo = UserInfoContextHolder.getUserInfo();
            if (Objects.isNull(userInfo)) userInfo = new UserInfo();
        } catch (Exception ignored) {}

        this.strictInsertFill(metaObject, "createBy", Long.class, userInfo.getId()); // 起始版本 3.3.0(推荐使用)
        this.strictInsertFill(metaObject, "updateBy", Long.class, userInfo.getId()); // 起始版本 3.3.0(推荐使用)

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now()); // 起始版本 3.3.0(推荐使用)
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now()); // 起始版本 3.3.0(推荐使用)
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        UserInfo userInfo = null;
        try {
            userInfo = UserInfoContextHolder.getUserInfo();
            if (Objects.isNull(userInfo)) userInfo = new UserInfo();
        } catch (Exception ignored) {}

        this.strictUpdateFill(metaObject, "updateBy", Long.class, userInfo.getId()); // 起始版本 3.3.0(推荐使用)
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class,  LocalDateTime.now()); // 起始版本 3.3.0(推荐)
    }

}
