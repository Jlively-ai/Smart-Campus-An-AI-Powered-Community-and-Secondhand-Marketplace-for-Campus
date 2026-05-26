package com.mrxu.stucomplarear2.config;

import com.mrxu.stucomplarear2.utils.response.Result;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public Result handleUnauthenticated(UnauthenticatedException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return Result.fail(401, "未登录或登录已过期", null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result handleUnauthorized(UnauthorizedException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return Result.fail(403, "没有权限", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result handleAuthentication(AuthenticationException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return Result.fail(401, e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletResponse response) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(500, "服务器内部错误: " + e.getMessage(), null);
    }
}
