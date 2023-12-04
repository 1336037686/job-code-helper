package com.lgx.codehelper.module.auth.model.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-03 13:03
 */
@Data
public class UserInfoResponse {

    private List<String> roles = List.of("admin");

    private String introduction = "I am a super administrator";

    private String avatar = "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif";

    private String name = "Super Admin";

}
