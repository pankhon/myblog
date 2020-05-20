/*
+--------------------------------------------------------------------------
|   Mblog [#RELEASE_VERSION#]
|   ========================================
|   Copyright (c) 2014, 2015 mtons. All Rights Reserved
|   http://www.mtons.com
|
+---------------------------------------------------------------------------
*/
package com.mtons.mblog.web.controller.site.auth;

import com.mtons.mblog.base.lang.Result;
import com.mtons.mblog.modules.data.AccountProfile;
import com.mtons.mblog.web.controller.BaseController;
import com.mtons.mblog.web.controller.site.Views;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录

 */
@Controller
public class LoginController extends BaseController {

    /**
     * 跳转登录页
     * @return
     */
	@GetMapping(value = "/login")
	public String view() {
		return view(Views.LOGIN);
	}

    /**
     * 提交登录
     * @param username
     * @param password
     * @param model
     * 参数rememberMe判断是否为记住登录，0未不记住，1为记住登录
     * @return
     */

    @PostMapping(value = "/login")
    public String login(String username,
                        String password,
                        @RequestParam(value = "rememberMe",defaultValue = "0") Boolean rememberMe,
                        ModelMap model) {
        //获取登录解析url路径/auth/login后，跳转到登陆页面
        String view = view(Views.LOGIN);

        //执行登录验证操作，通过shiro验证登录成功后，返回封装好的消息对象
        Result<AccountProfile> result = executeLogin(username, password, rememberMe);

        //判断是否成功登录，进行页面跳转操作
        if (result.isOk()) {

            //验证成功后，进行页面的重定向 redirect:/users/%d
            // 进入到UsersController，进行逻辑数据处理，最后返回个人主页视图界面
            view = String.format(Views.REDIRECT_USER_HOME, result.getData().getId());
        } else {
            model.put("message", result.getMessage());
        }
        return view;
    }

}
