package com.mtons.mblog.shiro.tags;

/**
 * <p>Equivalent to {@link org.apache.shiro.web.tags.LacksRoleTag}</p>
 * 缺少的角色
 */
public class LacksRoleTag extends RoleTag {
    protected boolean showBody(String roleName) {
        boolean hasRole = getSubject() != null && getSubject().hasRole(roleName);
        return !hasRole;
    }
}
