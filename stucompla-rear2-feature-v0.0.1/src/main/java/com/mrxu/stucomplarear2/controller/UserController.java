package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.dto.LoginDto;
import com.mrxu.stucomplarear2.dto.RegisterDto;
import com.mrxu.stucomplarear2.dto.UserEditDto;
import com.mrxu.stucomplarear2.dto.UserFindDto;
import com.mrxu.stucomplarear2.entity.Admin;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.AdminMapper;
import com.mrxu.stucomplarear2.service.UserService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.redis.RedisUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private AdminMapper adminMapper;
    @Value("${mrxu.stucomplarear2.image.save-path}")
    private String imageSavePath;

    @ApiOperation("注册")
    @PostMapping("/register")
    public Object register(@RequestBody RegisterDto registerDto) {
        //初始化返回值
        String regResult = userService.register(registerDto);
        if (regResult.equals("success")) {
            return Result.succ(200, regResult, null);
        } else {
            return Result.fail(regResult);
        }
    }

    @ApiOperation("修改密码")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/changePassword")
    public Result changePassword(String oldPassword, String inPassword, String secondPassword, ServletRequest request) {
        String regResult = userService.changePassword(request, oldPassword, inPassword, secondPassword);
        if (regResult.equals("success")) {
            return Result.succ(200, regResult, null);
        } else {
            return Result.fail(regResult);
        }
    }

    @ApiOperation("重置用户密码")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/changePwdByAdmin")
    public Result changePwdByAdmin(String newPassword, String secondPassword, String userId) {
        Result result = userService.changePwdByAdmin(newPassword, secondPassword, userId);
        return result;
    }

    @ApiOperation("锁定用户")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/lockedUser")
    public Result lockedUser(String userId, String cause) {
        Result result = userService.lockedUser(userId, cause);
        return result;
    }

    @ApiOperation("解锁用户")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/unLockUser")
    public Result unLockUser(String userId) {
        Result result = userService.unLockUser(userId);
        return result;
    }

    @ApiOperation("编辑个人信息")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/editUserInfo")
    public Result editUserInfo(@RequestBody UserEditDto userEditDto, HttpServletRequest request) {
        Result result = userService.editUserInfo(userEditDto, request);
        return result;
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginDto loginDto, ServletResponse response) {
        // 查询数据库获取用户信息
        User userFromDb = userService.getUserByUsername(loginDto.getUsername());
        // 用户不存在
        if (userFromDb == null) {
            return Result.fail("用户不存在！");
//            throw new UnknownAccountException("用户不存在！");
        }
        // 用户被锁定
        if (userFromDb.getLocked()) {
            return Result.fail("该用户已被锁定,暂时无法登录！请联系管理员1452162669@qq.com");
//            throw new LockedAccountException("该用户已被锁定,暂时无法登录！");
        }
        String inPassword = loginDto.getPassword()/*String.valueOf(new SimpleHash("SHA-1",
                loginDto.getPassword(), //输入的原始密码
                userFromDb.getUserId().toString(),//用户Id当盐值
                16))*/;
        if (!inPassword.equals(userFromDb.getPassword())) {
            return Result.fail("用户名或密码错误！");
//            throw new IllegalArgumentException("用户名或密码错误！");
        }
        long currentTimeMillis = System.currentTimeMillis();
        String token = JWTUtil.createToken(String.valueOf(userFromDb.getUserId()), currentTimeMillis, "user");
        redisUtil.set("User" + userFromDb.getUserId(), currentTimeMillis, 60 * 30);//redis里存30分钟
        ((HttpServletResponse) response).setHeader("Authorization", token);
        ((HttpServletResponse) response).setHeader("Access-Control-Expose-Headers", "Authorization");//前端可以拿到这个响应头
        return Result.succ(200, "登陆成功", token);
    }

    @ApiOperation("无权限")
    @GetMapping(path = "/unauthorized/{message}")
    public Result unauthorized(@PathVariable String message) {
        return Result.fail(message);
    }

    @ApiOperation("登出")
    @DeleteMapping("/logout")
    @RequiresAuthentication
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        redisUtil.del(userId);
        return Result.succ("退出成功");
    }

    @ApiOperation("获取用户列表")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result listUser(UserFindDto userFindDto) {
        Map<String, Object> map = userService.findUserList(userFindDto);
        return Result.succ(map);
    }

    @ApiOperation("获取用户总数")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getUserTotal")
    public Result getUserTotal() {
        Result result = userService.getUserTotal();
        return result;
    }

    @ApiOperation("获取个人信息")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/info")
    public Result getInfo(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(jwt);
        User user = userService.getUserByUserId(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        return Result.succ(user);
    }

    @ApiOperation("上传头像")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/uploadAvatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) return Result.fail("文件不能为空");
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = java.util.UUID.randomUUID().toString().replace("-", "") + extension;
            String savePath = imageSavePath;
            java.io.File dir = new java.io.File(savePath);
            if (!dir.exists()) dir.mkdirs();
            java.io.File dest = new java.io.File(savePath, fileName);
            file.transferTo(dest);
            String avatarUrl = "/images/" + fileName;
            User user = userService.getUserByUserId(userId);
            user.setAvatar(avatarUrl);
            userService.updateById(user);
            return Result.succ(avatarUrl);
        } catch (Exception e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    @ApiOperation("注销账号")
    @RequiresRoles(value = {"user"}, logical = Logical.OR)
    @PostMapping("/deactivate")
    public Result deactivateAccount(HttpServletRequest request) {
        return userService.deactivateAccount(request);
    }

    @ApiOperation("获取用户公开信息")
    @GetMapping("/publicInfo/{userId}")
    public Result getPublicInfo(@PathVariable("userId") String userId) {
        Map<String, Object> info = new HashMap<>();
        // First try User table
        User user = userService.getById(userId);
        if (user != null) {
            // 已注销用户显示用户已注销
            if (user.getStatus() != null && user.getStatus() == 2) {
                info.put("userId", user.getUserId());
                info.put("nickname", "用户已注销");
                info.put("avatar", null);
                info.put("signature", "该账号已注销");
                info.put("createTime", user.getCreateTime());
                info.put("deactivated", true);
                return Result.succ(info);
            }
            info.put("userId", user.getUserId());
            info.put("username", user.getUsername());
            info.put("nickname", user.getNickname());
            info.put("avatar", user.getAvatar());
            info.put("sex", user.getSex());
            info.put("signature", user.getSignature());
            info.put("createTime", user.getCreateTime());
            // Check if this user is also an admin
            Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", user.getUsername()).last("LIMIT 1"));
            if (admin != null) {
                info.put("roleName", admin.getRoleId() != null && admin.getRoleId() == 1 ? "super" : "admin");
            }
            return Result.succ(info);
        }
        // If not found in User table, try Admin table by adminId
        Admin admin = adminMapper.selectById(userId);
        if (admin != null) {
            info.put("userId", admin.getAdminId());
            info.put("username", admin.getUsername());
            info.put("nickname", admin.getUsername());
            info.put("avatar", null);
            info.put("sex", null);
            info.put("signature", "管理员");
            info.put("createTime", admin.getCreateTime());
            info.put("roleName", admin.getRoleId() != null && admin.getRoleId() == 1 ? "super" : "admin");
            return Result.succ(info);
        }
        return Result.fail("用户不存在");
    }

    @ApiOperation("批量获取用户公开信息")
    @GetMapping("/batchInfo")
    public Result batchInfo(@RequestParam("ids") String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return Result.succ(new ArrayList<>());
        }
        String[] idArr = ids.split(",");
        List<Map<String, Object>> result = new ArrayList<>();
        for (String id : idArr) {
            String uid = id.trim();
            if (uid.isEmpty()) continue;
            // First try User table
            User user = userService.getById(uid);
            if (user != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("userId", user.getUserId());
                if (user.getStatus() != null && user.getStatus() == 2) {
                    info.put("nickname", "用户已注销");
                    info.put("avatar", null);
                } else {
                    info.put("nickname", user.getNickname());
                    info.put("avatar", user.getAvatar());
                }
                result.add(info);
                continue;
            }
            // If not found in User table, try Admin table
            Admin admin = adminMapper.selectById(uid);
            if (admin != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("userId", admin.getAdminId());
                info.put("nickname", admin.getUsername());
                info.put("avatar", null);
                result.add(info);
            }
        }
        return Result.succ(result);
    }

    @ApiOperation("搜索用户")
    @GetMapping("/search")
    public Result searchUsers(@RequestParam(required = false) String keyword) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Support searching with ID prefix like "USR-xxx" - strip prefix for matching
            final String kw = keyword.trim();
            String temp = kw;
            if (temp.contains("-")) {
                String[] parts = temp.split("-", 2);
                if (parts.length > 1) {
                    temp = parts[1];
                }
            }
            final String cleanKw = temp;
            qw.and(wrapper -> wrapper
                .like("nickname", kw)
                .or().like("username", kw)
                .or().like("user_id", kw)
                .or().like("user_id", cleanKw)
            );
        }
        qw.ne("status", 2);
        qw.last("LIMIT 20");
        List<User> users = userService.list(qw);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> info = new HashMap<>();
            info.put("userId", user.getUserId());
            info.put("nickname", user.getStatus() != null && user.getStatus() == 2 ? "用户已注销" : user.getNickname());
            info.put("avatar", user.getStatus() != null && user.getStatus() == 2 ? null : user.getAvatar());
            info.put("username", user.getUsername());
            result.add(info);
        }
        return Result.succ(result);
    }

}
