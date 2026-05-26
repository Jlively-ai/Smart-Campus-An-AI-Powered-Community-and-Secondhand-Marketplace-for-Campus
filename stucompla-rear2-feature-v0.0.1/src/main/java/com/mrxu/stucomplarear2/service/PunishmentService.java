package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.PunishmentDto;
import com.mrxu.stucomplarear2.entity.Punishment;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface PunishmentService extends IService<Punishment> {
    Result createPunishment(PunishmentDto punishmentDto, HttpServletRequest request);
    Map<String, Object> findPunishmentList(Integer pageNum, Integer pageSize, String userId, String type, Integer status);
    List<Punishment> getActivePunishments(String userId);
    boolean isUserMuted(String userId);
    String getMuteReason(String userId);
    Result liftPunishment(String punishmentId);
    List<Punishment> getPublicPunishments(String userId);
    Result appeal(String punishmentId, String appealReason, String userId);
    Result handleAppeal(String punishmentId, Integer appealState, String appealResult);
    List<Punishment> getMyPunishments(String userId);
}
