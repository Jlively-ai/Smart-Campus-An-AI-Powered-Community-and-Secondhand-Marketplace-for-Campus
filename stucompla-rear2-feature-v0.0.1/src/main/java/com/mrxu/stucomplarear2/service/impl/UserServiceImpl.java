package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrxu.stucomplarear2.dto.RegisterDto;
import com.mrxu.stucomplarear2.dto.UserEditDto;
import com.mrxu.stucomplarear2.dto.UserFindDto;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.UserService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.redis.RedisUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Result login(String username, String password, HttpServletResponse response) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", password);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            return Result.fail("username or password error");
        }
        if (user.getLocked()) {
            return Result.fail("account locked");
        }
        long currentTimeMillis = System.currentTimeMillis();
        String token = JWTUtil.createToken(String.valueOf(user.getUserId()), currentTimeMillis, "user");
        redisUtil.set("User" + user.getUserId(), currentTimeMillis, 60 * 30);
        response.setHeader("Authorization", token);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        return Result.succ(200, "login success", token);
    }

    @Override
    public String register(RegisterDto registerDto) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerDto.getUsername());
        User existUser = this.getOne(queryWrapper);
        if (existUser != null) {
            return "username exists";
        }
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(registerDto.getPassword());
        user.setNickname(registerDto.getUsername());
        user.setSex(registerDto.getSex() != null ? registerDto.getSex() : "male");
        user.setLocked(false);
        user.setStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setUserId(com.mrxu.stucomplarear2.utils.IdGenerator.generateId(com.mrxu.stucomplarear2.utils.IdGenerator.USER));
        this.save(user);
        return "success";
    }

    @Override
    public String changePassword(ServletRequest request, String oldPassword, String inPassword, String secondPassword) {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        User user = this.getById(userId);
        if (!oldPassword.equals(user.getPassword())) {
            return "old password error";
        }
        if (!inPassword.equals(secondPassword)) {
            return "passwords not match";
        }
        user.setPassword(inPassword);
        user.setUpdateTime(new Date());
        this.updateById(user);
        return "success";
    }

    @Override
    public Result changePwdByAdmin(String newPassword, String secondPassword, String userId) {
        if (!newPassword.equals(secondPassword)) {
            return Result.fail("passwords not match");
        }
        User user = this.getById(userId);
        if (user == null) {
            return Result.fail("user not found");
        }
        user.setPassword(newPassword);
        user.setUpdateTime(new Date());
        this.updateById(user);
        return Result.succ("success");
    }

    @Override
    public Result lockedUser(String userId, String cause) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.fail("user not found");
        }
        user.setLocked(true);
        user.setStatus(1);
        this.updateById(user);
        // 清除Redis中的登录状态，强制下线
        redisUtil.del("User" + userId);
        return Result.succ("success");
    }

    @Override
    public Result unLockUser(String userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.fail("user not found");
        }
        user.setLocked(false);
        user.setStatus(0);
        this.updateById(user);
        return Result.succ("success");
    }

    @Override
    public Result editUserInfo(UserEditDto userEditDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        User user = this.getById(userId);
        if (user == null) {
            return Result.fail("user not found");
        }
        if (userEditDto.getUsername() != null) {
            user.setUsername(userEditDto.getUsername());
        }
        if (userEditDto.getSex() != null) {
            user.setSex(userEditDto.getSex());
        }
        if (userEditDto.getAvatar() != null) {
            user.setAvatar(userEditDto.getAvatar());
        }
        if (userEditDto.getSignature() != null) {
            user.setSignature(userEditDto.getSignature());
        }
        user.setUpdateTime(new Date());
        this.updateById(user);
        return Result.succ("success");
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.getOne(queryWrapper);
    }

    @Override
    public User getUserByUserId(String userId) {
        return this.getById(userId);
    }

    @Override
    public Map<String, Object> findUserList(UserFindDto userFindDto) {
        int pageNum = userFindDto.getPageNum() != null ? userFindDto.getPageNum() : 1;
        int pageSize = userFindDto.getPageSize() != null ? userFindDto.getPageSize() : 10;
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userFindDto.getUsername() != null && !userFindDto.getUsername().isEmpty()) {
            queryWrapper.like("username", userFindDto.getUsername());
        }
        if (userFindDto.getSex() != null && !userFindDto.getSex().isEmpty()) {
            queryWrapper.eq("sex", userFindDto.getSex());
        }
        if (userFindDto.getStatus() != null) {
            queryWrapper.eq("status", userFindDto.getStatus());
        }
        queryWrapper.orderByDesc("create_time");
        Page<User> userPage = this.page(page, queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", userPage.getTotal());
        map.put("records", userPage.getRecords());
        return map;
    }

    @Override
    public Result getUserTotal() {
        long total = this.count();
        return Result.succ(total);
    }

    @Override
    public Result deactivateAccount(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        User user = this.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 2) {
            return Result.fail("账号已注销");
        }
        user.setStatus(2); // 2=已注销
        user.setNickname("用户已注销");
        user.setAvatar(null);
        user.setSignature("该账号已注销");
        user.setLocked(true);
        user.setUpdateTime(new Date());
        this.updateById(user);
        // 清除Redis中的登录状态
        redisUtil.del("User" + userId);
        return Result.succ("账号已注销");
    }
}
