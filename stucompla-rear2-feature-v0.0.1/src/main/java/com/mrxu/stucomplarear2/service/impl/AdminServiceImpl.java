package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.AdminFindDto;
import com.mrxu.stucomplarear2.entity.Admin;
import com.mrxu.stucomplarear2.mapper.AdminMapper;
import com.mrxu.stucomplarear2.service.AdminService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.redis.RedisUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Result login(String username, String password, HttpServletResponse response) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", password);
        Admin admin = this.getOne(queryWrapper);
        if (admin == null) {
            return Result.fail("username or password error");
        }
        long currentTimeMillis = System.currentTimeMillis();
        String role = admin.getRoleId() != null && admin.getRoleId() == 1 ? "super" : "admin";
        String token = JWTUtil.createToken(String.valueOf(admin.getAdminId()), currentTimeMillis, role);
        redisUtil.set("Admin" + admin.getAdminId(), currentTimeMillis, 60 * 30);
        response.setHeader("Authorization", token);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        return Result.succ(200, "login success", token);
    }

    @Override
    public Admin getAdminByAdminId(String adminId) {
        return this.getById(adminId);
    }

    @Override
    public Map<String, Object> findAdminList(AdminFindDto adminFindDto) {
        int pageNum = adminFindDto.getPageNum() != null ? adminFindDto.getPageNum() : 1;
        int pageSize = adminFindDto.getPageSize() != null ? adminFindDto.getPageSize() : 10;
        Page<Admin> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        if (adminFindDto.getUsername() != null && !adminFindDto.getUsername().isEmpty()) {
            queryWrapper.like("username", adminFindDto.getUsername());
        }
        if (adminFindDto.getRoleId() != null) {
            queryWrapper.eq("role_id", adminFindDto.getRoleId());
        }
        queryWrapper.orderByDesc("create_time");
        Page<Admin> adminPage = this.page(page, queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", adminPage.getTotal());
        map.put("records", adminPage.getRecords());
        return map;
    }

    @Override
    public Result addAdmin(String username, String password, Integer roleId) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        if (this.getOne(queryWrapper) != null) {
            return Result.fail("username exists");
        }
        Admin admin = new Admin();
        admin.setAdminId(IdGenerator.generateId(IdGenerator.ADMIN));
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setRoleId(roleId);
        admin.setCreateTime(new Date());
        this.save(admin);
        return Result.succ("success");
    }

    @Override
    public Result deleteAdmin(String adminId) {
        this.removeById(adminId);
        return Result.succ("success");
    }

    @Override
    public Result changePassword(String oldPassword, String inPassword, String secondPassword, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String adminId = JWTUtil.getUserId(token);
        Admin admin = this.getById(adminId);
        if (!oldPassword.equals(admin.getPassword())) {
            return Result.fail("old password error");
        }
        if (!inPassword.equals(secondPassword)) {
            return Result.fail("passwords not match");
        }
        admin.setPassword(inPassword);
        this.updateById(admin);
        return Result.succ("success");
    }

    @Override
    public Result changeMyUsername(String username, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String adminId = JWTUtil.getUserId(token);
        Admin admin = this.getById(adminId);
        if (admin == null) {
            return Result.fail("admin not found");
        }
        admin.setUsername(username);
        this.updateById(admin);
        return Result.succ("success");
    }

    @Override
    public Result changeRole(String adminId, Integer roleId) {
        Admin admin = this.getById(adminId);
        if (admin == null) {
            return Result.fail("admin not found");
        }
        admin.setRoleId(roleId);
        this.updateById(admin);
        return Result.succ("success");
    }

    @Override
    public Result changePermissions(String adminId, String permissions) {
        Admin admin = this.getById(adminId);
        if (admin == null) {
            return Result.fail("admin not found");
        }
        admin.setPermissions(permissions);
        this.updateById(admin);
        return Result.succ("success");
    }
}
