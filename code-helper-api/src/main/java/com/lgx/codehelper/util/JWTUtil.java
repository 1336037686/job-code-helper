package com.lgx.codehelper.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import com.lgx.codehelper.common.filter.auth.UserInfo;
import com.lgx.codehelper.common.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * @author LGX_TvT <br>
 * @version 1.0 <br>
 * Create by 2022-04-13 01:16 <br>
 * @description: TokenUtil <br>
 */
@Slf4j
public class JWTUtil {

    public final static String SYS_JWT_TOKEN_PAYLOAD_USERID = "id";
    public final static String SYS_JWT_TOKEN_PAYLOAD_USERNAME = "username";

    /**
     * 创建AccessToken
     * @param userInfo /
     * @return /
     */
    public static String createToken(UserInfo userInfo) {
        return createAccessToken(userInfo.getId(), userInfo.getUsername());
    }

    /**
     * 创建AccessToken
     * @param username /
     * @return /
     */
    public static String createAccessToken(Long userId, String username) {
        JwtProperties jwtConfig = SpringUtil.getBean(JwtProperties.class);

        DateTime now = DateTime.now();
        DateTime newTime = now.offsetNew(DateField.SECOND, jwtConfig.getAccessTokenExpiration().intValue());

        Map<String,Object> payload = new HashMap<>();
        //签发时间
        payload.put(JWTPayload.ISSUED_AT, now);
        //过期时间
        payload.put(JWTPayload.EXPIRES_AT, newTime);
        //生效时间
        payload.put(JWTPayload.NOT_BEFORE, now);
        //载荷
        payload.put(SYS_JWT_TOKEN_PAYLOAD_USERID, userId);
        payload.put(SYS_JWT_TOKEN_PAYLOAD_USERNAME, username);
        return cn.hutool.jwt.JWTUtil.createToken(payload, jwtConfig.getSecretKey().getBytes());
    }

    /**
     * 解析Token
     * @param token /
     * @return /
     */
    public static UserInfo parseToken(String token) {
        JwtProperties jwtConfig = SpringUtil.getBean(JwtProperties.class);
        JWT jwt = cn.hutool.jwt.JWTUtil.parseToken(token).setKey(jwtConfig.getSecretKey().getBytes());
        Object username = jwt.getPayload(SYS_JWT_TOKEN_PAYLOAD_USERNAME);
        Object userId = jwt.getPayload(SYS_JWT_TOKEN_PAYLOAD_USERID);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(Long.valueOf(userId.toString()));
        userInfo.setUsername(username.toString());
        return userInfo;
    }

    /**
     * 校验Token
     * @param token /
     * @return /
     */
    public static boolean verify(String token) {
        if (StringUtils.isBlank(token)) return false;
        JwtProperties jwtConfig = SpringUtil.getBean(JwtProperties.class);
        JWT jwt = cn.hutool.jwt.JWTUtil.parseToken(token);
        jwt.setKey(jwtConfig.getSecretKey().getBytes());
        return jwt.validate(0) && jwt.verify();
    }

    /**
     * 校验Token是否过期
     * @param token /
     * @return /
     */
    public static boolean validateTime(String token) {
        if (StringUtils.isBlank(token)) return false;
        JwtProperties jwtConfig = SpringUtil.getBean(JwtProperties.class);
        JWT jwt = cn.hutool.jwt.JWTUtil.parseToken(token);
        return jwt.setKey(jwtConfig.getSecretKey().getBytes()).validate(0);
    }

    /**
     * 校验Token是否有效
     * @param token /
     * @return /
     */
    public static boolean validateLegal(String token) {
        if (StringUtils.isBlank(token)) return false;
        JwtProperties jwtConfig = SpringUtil.getBean(JwtProperties.class);
        JWT jwt = cn.hutool.jwt.JWTUtil.parseToken(token);
        return jwt.setKey(jwtConfig.getSecretKey().getBytes()).verify();
    }
}
