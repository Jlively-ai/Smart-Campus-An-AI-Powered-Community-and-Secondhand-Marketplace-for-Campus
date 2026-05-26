package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.ReportDto;
import com.mrxu.stucomplarear2.dto.ReportFindDto;
import com.mrxu.stucomplarear2.entity.Report;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface ReportService extends IService<Report> {
    Result submitReport(ReportDto reportDto, HttpServletRequest request);
    Map<String, Object> findReportList(ReportFindDto reportFindDto);
    Result handleReport(String reportId, Integer status, String handleResult, HttpServletRequest request);
    Result handleReport(String reportId, Integer status, String handleResult, String punishType, String lockReason, String targetType, String targetId, HttpServletRequest request);
    List<Report> getMyReports(String userId);
}
