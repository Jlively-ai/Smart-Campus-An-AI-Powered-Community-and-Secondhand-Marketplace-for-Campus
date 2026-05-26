package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.dto.ReportDto;
import com.mrxu.stucomplarear2.dto.ReportFindDto;
import com.mrxu.stucomplarear2.service.ReportService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mrxu.stucomplarear2.entity.Report;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @ApiOperation("提交举报")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/submit")
    public Result submitReport(@RequestBody ReportDto reportDto, HttpServletRequest request) {
        return reportService.submitReport(reportDto, request);
    }

    @ApiOperation("获取举报列表（管理员）")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result getReportList(ReportFindDto reportFindDto) {
        Map<String, Object> map = reportService.findReportList(reportFindDto);
        return Result.succ(map);
    }

    @ApiOperation("处理举报（管理员）")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/handle")
    public Result handleReport(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String reportId = (String) params.get("reportId");
        Integer status = (Integer) params.get("status");
        String handleResult = (String) params.get("handleResult");
        String punishType = (String) params.get("punishType");
        String lockReason = (String) params.get("lockReason");
        String targetType = (String) params.get("targetType");
        String targetId = (String) params.get("targetId");
        return reportService.handleReport(reportId, status, handleResult, punishType, lockReason, targetType, targetId, request);
    }

    @ApiOperation("查询我的举报记录")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/my")
    public Result getMyReports(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        List<Report> list = reportService.getMyReports(userId);
        return Result.succ(list);
    }
}
