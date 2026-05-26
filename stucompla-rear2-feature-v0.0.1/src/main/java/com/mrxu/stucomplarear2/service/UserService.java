package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.RegisterDto;
import com.mrxu.stucomplarear2.dto.UserEditDto;
import com.mrxu.stucomplarear2.dto.UserFindDto;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface UserService extends IService<User> {

    Result login(String username, String password, HttpServletResponse response);

    String register(RegisterDto registerDto);

    String changePassword(ServletRequest request, String oldPassword, String inPassword, String secondPassword);

    Result changePwdByAdmin(String newPassword, String secondPassword, String userId);

    Result lockedUser(String userId, String cause);

    Result unLockUser(String userId);

    Result editUserInfo(UserEditDto userEditDto, HttpServletRequest request);

    User getUserByUsername(String username);

    User getUserByUserId(String userId);

    Map<String, Object> findUserList(UserFindDto userFindDto);

    Result getUserTotal();

    Result deactivateAccount(HttpServletRequest request);
}
