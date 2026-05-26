package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.LetterAddDto;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;

public interface LetterService extends IService<Letter> {

    Result add(LetterAddDto letterAddDto, HttpServletRequest request);

    Result getMySessionList(HttpServletRequest request);

    Result getLetterListBySessionId(String sessionId, HttpServletRequest request);

    Result getMyNoticeList(HttpServletRequest request);

    Result getMyUnReadTotal(HttpServletRequest request);

    Result getMyUnReadLetterTotal(HttpServletRequest request);

    Result getMyUnReadNoticeTotal(HttpServletRequest request);

    Result myMessageList(String type, Integer pageNum, Integer pageSize, HttpServletRequest request);

    Result markRead(String letterId, HttpServletRequest request);

    Result markAllRead(String type, HttpServletRequest request);

    Result unreadCountByType(HttpServletRequest request);

    void sendSystemNotification(String receiverId, String content, String messageType);

    void sendSystemNotification(String receiverId, String content, String messageType, String targetType, String targetId);
}
