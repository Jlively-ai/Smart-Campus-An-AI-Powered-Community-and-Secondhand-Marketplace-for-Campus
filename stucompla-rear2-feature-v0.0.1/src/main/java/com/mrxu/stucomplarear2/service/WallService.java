package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.WallApplyDto;
import com.mrxu.stucomplarear2.dto.WallAuditDto;
import com.mrxu.stucomplarear2.dto.WallEditDto;
import com.mrxu.stucomplarear2.dto.WallFindDto;
import com.mrxu.stucomplarear2.entity.Wall;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface WallService extends IService<Wall> {

    Result apply(WallApplyDto wallApplyDto, HttpServletRequest request);

    String audit(WallAuditDto wallAuditDto, HttpServletRequest request);

    Map<String, Object> findWall(WallFindDto wallFindDto, HttpServletRequest request);

    Map<String, Object> findWall(WallFindDto wallFindDto, boolean hideAnonymousUserInfo, HttpServletRequest request);

    Result editWall(WallEditDto wallEditDto, HttpServletRequest request);

    Result findMyWall(WallFindDto wallFindDto, HttpServletRequest request);

    Result getWallTotal();

    Result getWallData();
}
