package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.entity.Announcement;
import com.mrxu.stucomplarear2.service.AnnouncementService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @ApiOperation("发布公告")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/publish")
    public Result publish(@RequestBody Announcement announcement, HttpServletRequest request) {
        return announcementService.publish(announcement, request);
    }

    @ApiOperation("修改公告")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/update")
    public Result update(@RequestBody Announcement announcement) {
        return announcementService.updateAnnouncement(announcement);
    }

    @ApiOperation("删除公告")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/delete")
    public Result delete(String announcementId) {
        return announcementService.deleteAnnouncement(announcementId);
    }

    @ApiOperation("管理端公告列表")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result list(Integer pageNum, Integer pageSize) {
        return announcementService.findList(pageNum, pageSize);
    }

    @ApiOperation("公开公告列表")
    @GetMapping("/publicList")
    public Result publicList(Integer pageNum, Integer pageSize) {
        return announcementService.findPublicList(pageNum, pageSize);
    }

    @ApiOperation("浏览公告（浏览量+1）")
    @GetMapping("/view/{announcementId}")
    public Result viewAnnouncement(@PathVariable("announcementId") String announcementId) {
        Announcement announcement = announcementService.getById(announcementId);
        if (announcement == null) return Result.fail("不存在");
        announcement.setViewNum(announcement.getViewNum() != null ? announcement.getViewNum() + 1 : 1);
        announcementService.updateById(announcement);
        return Result.succ(announcement.getViewNum());
    }
}
