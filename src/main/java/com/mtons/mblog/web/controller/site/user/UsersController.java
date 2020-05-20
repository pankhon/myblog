
package com.mtons.mblog.web.controller.site.user;

import com.mtons.mblog.base.lang.MtonsException;
import com.mtons.mblog.modules.data.AccountProfile;
import com.mtons.mblog.modules.service.MessageService;
import com.mtons.mblog.modules.service.UserService;
import com.mtons.mblog.web.controller.BaseController;
import com.mtons.mblog.web.controller.site.Views;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户我的主页
 *
 */
@Controller
@RequestMapping("/users")
public class UsersController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;

    /**
     * 用户文章
     * @param userId 用户ID
     * @param model  ModelMap
     * @return template name
     */
    @GetMapping(value = "/{userId}")
    public String posts(@PathVariable(value = "userId") Long userId,
                        ModelMap model, HttpServletRequest request) {
        //进入method方法，第二个参数method=posts
        return method(userId, Views.METHOD_POSTS, model, request);
    }

    /**
     * 通用方法, 访问 users 目录下的页面
     * @param userId 用户ID
     * @param method 调用方法
     * @param model  ModelMap
     * @return template name
     */
    @GetMapping(value = "/{userId}/{method}")
    public String method(@PathVariable(value = "userId") Long userId,
                         @PathVariable(value = "method") String method,
                         ModelMap model, HttpServletRequest request) {
        model.put("pageNo", ServletRequestUtils.getIntParameter(request, "pageNo", 1));

        // 访问消息页,判断此方法是否为message
        if (Views.METHOD_MESSAGES.equals(method)) {
            /**
             * 标记已读，通过shiro，获取用户登录的信息
             * 返回当前用户
             */
            AccountProfile profile = getProfile();
            //判断通过shiro 返回的信息，若为null，说明用户未登录，或者输入的用户id不与当前登录的匹配
            //要求重新输入
            if (null == profile || profile.getId() != userId) {
                throw new MtonsException("您没有权限访问该页面");
            }
            messageService.readed4Me(profile.getId());
        }

        //加载用户数据
        initUser(userId, model);

        /**
         * /user/method_
         * 生成对应的映射路径，用户发表的评论路径:/user/method_comments,跳转视图进入method_comments.ftl进入评论页面
         *  生成 view("/user/method_comments")
         */
        return view(String.format(Views.USER_METHOD_TEMPLATE, method));
    }

    private void initUser(long userId, ModelMap model) {
        model.put("user", userService.get(userId));
        boolean owner = false;

        //获取账号信息
        AccountProfile profile = getProfile();
        if (null != profile && profile.getId() == userId) {
            owner = true;

            /**
             * 1、调用基类BaseController控制器的putProfile
             * 2、将查到的账号信息放入session中，将属性命名为setAttribute("profile", profile)
             * 3、未读消息的获取也在 userService.findProfile方法中加载
             */
            putProfile(userService.findProfile(profile.getId()));
        }
        model.put("owner", owner);
    }

}
