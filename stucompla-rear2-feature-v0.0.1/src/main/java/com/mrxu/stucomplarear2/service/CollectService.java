package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.CollectFindDto;
import com.mrxu.stucomplarear2.entity.Collect;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;

public interface CollectService extends IService<Collect> {

    Result checkCollect(String postId, HttpServletRequest request);

    Result add(String postId, HttpServletRequest request);

    Result deleteCollect(String postId, HttpServletRequest request);

    Result listCollect(CollectFindDto collectFindDto);
}
