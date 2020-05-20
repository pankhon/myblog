package com.mtons.mblog.shiro;

import com.mtons.mblog.base.lang.Consts;
import com.mtons.mblog.modules.data.AccountProfile;
import com.mtons.mblog.modules.data.UserVO;
import com.mtons.mblog.modules.entity.Role;
import com.mtons.mblog.modules.service.UserRoleService;
import com.mtons.mblog.modules.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AccountRealm extends AuthorizingRealm {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;

    public AccountRealm() {
        super(new AllowAllCredentialsMatcher());
        setAuthenticationTokenClass(UsernamePasswordToken.class);
    }

    /**
     * 执行权限受理逻辑，添加角色和权限
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        //获取当前登录的账户AccountProfile
        // 此当前用户对象来源于登录验证方法返回的SimpleAuthenticationInfo构造函数中的第一个参数Principal
        AccountProfile profile = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (profile != null) {
            UserVO user = userService.get(profile.getId());
            if (user != null) {

                //添加角色和权限
                SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
                //通过userRoleService获取所以角色的数据，返回的是角色数据集合
                List<Role> roles = userRoleService.listRoles(user.getId());

                //添加角色
                roles.forEach(role -> {
                    info.addRole(role.getName());

                    //添加权限
                    // getPermissions()返回的是权限集合List<Permission>
                    role.getPermissions().forEach(permission -> info.addStringPermission(permission.getName()));
                });
                return info;
            }
        }
        return null;
    }

    /**
     * 登录认证，点击登录后，Subject.login(token)，自动执行此方法
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //编写shiro判断逻辑，判断用户名和密码
        //1.判断用户名
        //token令牌中的用户信息是登录时候传进来的，即Subject.login(token)中的token令牌
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        //进行登录判断，token中用户名和密码与数据库中的作对比
        AccountProfile profile = getAccount(userService, token);

        //用户名不存在,shiro底层会抛出UnknownAccountException异常信息
        if (null == profile) {
            throw new UnknownAccountException(upToken.getUsername());
        }

        //用户的状态被禁用，则抛出LockedAccountException异常信息
        if (profile.getStatus() == Consts.STATUS_CLOSED) {
            throw new LockedAccountException(profile.getName());
        }

        //进行结果封装，token.getCredentials为md5加密后的密文
        //SimpleAuthenticationInfo是此方法返回类型AuthenticationInfo的实现类
        /**
         * 第一个参数为profile，当前账号对象
         * 第二个参数为token.getCredentials()，md5加密后的密码
         * 第三个对象为getName()，返回当前Realm域对象名称，认证名
         */
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(profile, token.getCredentials(), getName());
        Session session = SecurityUtils.getSubject().getSession();
        session.setAttribute("profile", profile);
        return info;
    }

    //利用token里面的用户名判断与数据库的密码是否相符，若相符，返回登录包装类用户数据
    protected AccountProfile getAccount(UserService userService, AuthenticationToken token) {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        //调用用户业务层方法进行登录判断，参数为token的用户名和密码
        return userService.login(upToken.getUsername(), String.valueOf(upToken.getPassword()));
    }
}
