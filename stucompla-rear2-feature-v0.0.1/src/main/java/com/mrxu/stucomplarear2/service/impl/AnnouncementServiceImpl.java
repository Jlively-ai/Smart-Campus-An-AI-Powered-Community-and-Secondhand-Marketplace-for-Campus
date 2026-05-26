package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.Announcement;
import com.mrxu.stucomplarear2.mapper.AnnouncementMapper;
import com.mrxu.stucomplarear2.service.AnnouncementService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public Result publish(Announcement announcement, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String adminId = JWTUtil.getUserId(token);
        announcement.setAdminId(adminId);
        announcement.setAnnouncementId(IdGenerator.generateId(IdGenerator.ANNOUNCEMENT));
        announcement.setStatus(1);
        announcement.setCreateTime(new Date());
        announcement.setUpdateTime(new Date());
        this.save(announcement);
        return Result.succ("success");
    }

    @Override
    public Result updateAnnouncement(Announcement announcement) {
        Announcement existing = this.getById(announcement.getAnnouncementId());
        if (existing == null) return Result.fail("公告不存在");
        if (announcement.getTitle() != null) existing.setTitle(announcement.getTitle());
        if (announcement.getContent() != null) existing.setContent(announcement.getContent());
        if (announcement.getAnnouncementType() != null) existing.setAnnouncementType(announcement.getAnnouncementType());
        if (announcement.getStatus() != null) existing.setStatus(announcement.getStatus());
        existing.setUpdateTime(new Date());
        this.updateById(existing);
        return Result.succ("success");
    }

    @Override
    public Result deleteAnnouncement(String announcementId) {
        this.removeById(announcementId);
        return Result.succ("success");
    }

    @Override
    public Result findList(Integer pageNum, Integer pageSize) {
        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 10;
        Page<Announcement> page = new Page<>(pn, ps);
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        Page<Announcement> result = this.page(page, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", result.getRecords());
        return Result.succ(map);
    }

    @Override
    public Result findPublicList(Integer pageNum, Integer pageSize) {
        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 10;
        Page<Announcement> page = new Page<>(pn, ps);
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("create_time");
        Page<Announcement> result = this.page(page, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", result.getRecords());
        return Result.succ(map);
    }
}
