package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.Announcement;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;

public interface AnnouncementService extends IService<Announcement> {
    Result publish(Announcement announcement, HttpServletRequest request);
    Result updateAnnouncement(Announcement announcement);
    Result deleteAnnouncement(String announcementId);
    Result findList(Integer pageNum, Integer pageSize);
    Result findPublicList(Integer pageNum, Integer pageSize);
}
