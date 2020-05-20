package com.mtons.mblog.shiro.tags;

/**
 * <p>Equivalent to {@link org.apache.shiro.web.tags.HasRoleTag}</p>
 * 具备角色
 */
public class HasRoleTag extends RoleTag {
    protected boolean showBody(String roleName) {
        return getSubject() != null && getSubject().hasRole(roleName);
    }
}
