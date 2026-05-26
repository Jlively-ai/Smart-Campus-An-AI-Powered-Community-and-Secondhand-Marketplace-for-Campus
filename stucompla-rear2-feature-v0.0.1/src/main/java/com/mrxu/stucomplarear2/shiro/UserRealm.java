package com.mrxu.stucomplarear2.shiro;

import com.mrxu.stucomplarear2.entity.Admin;
import com.mrxu.stucomplarear2.entity.Role;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.service.AdminService;
import com.mrxu.stucomplarear2.service.RoleService;
import com.mrxu.stucomplarear2.service.UserService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private RoleService roleService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String token = (String) principals.getPrimaryPrincipal();
        String userId = JWTUtil.getUserId(token);
        String role = JWTUtil.getRole(token);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<String> roles = new HashSet<>();
        if ("user".equals(role)) {
            roles.add("user");
        } else if ("super".equals(role)) {
            roles.add("super");
            roles.add("admin");
        } else if ("admin".equals(role)) {
            roles.add("admin");
        }
        info.setRoles(roles);

        // 设置细粒度权限
        Set<String> permissions = new HashSet<>();
        if ("super".equals(role)) {
            // 超级管理员拥有所有权限
            permissions.add("*");
        } else if ("admin".equals(role)) {
            // 普通管理员从数据库读取权限
            Admin admin = adminService.getAdminByAdminId(userId);
            if (admin != null && admin.getPermissions() != null && !admin.getPermissions().isEmpty()) {
                permissions.addAll(Arrays.asList(admin.getPermissions().split(",")));
            }
        }
        info.setStringPermissions(permissions);

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getPrincipal();
        String userId = JWTUtil.getUserId(token);
        String role = JWTUtil.getRole(token);
        if (userId == null) {
            throw new AuthenticationException("token无效");
        }
        if ("user".equals(role)) {
            User user = userService.getUserByUserId(userId);
            if (user == null) {
                throw new AuthenticationException("用户不存在");
            }
            if (user.getLocked() != null && user.getLocked()) {
                throw new AuthenticationException("账号已被锁定");
            }
        } else {
            Admin admin = adminService.getAdminByAdminId(userId);
            if (admin == null) {
                throw new AuthenticationException("管理员不存在");
            }
        }
        return new SimpleAuthenticationInfo(token, token, getName());
    }
}
