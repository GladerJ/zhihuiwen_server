package top.mygld.zhihuiwen_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.pojo.TotalReport;
import top.mygld.zhihuiwen_server.service.ReportService;
import top.mygld.zhihuiwen_server.service.TotalReportService;

@RestController
@RequestMapping("/totalReport")
public class TotalReportController {
    @Autowired
    private TotalReportService totalReportService;
    @RequestMapping("/getTotalReport")
    public Result<TotalReport> getTotalReport() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(totalReportService.selectTotalReportByUserId(userId));
    }
    @RequestMapping("/updateTotalReport")
    public Result<String> updateTotalReport(@RequestBody TotalReport totalReport) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        totalReport.setUserId(userId);
        totalReportService.updateTotalReport(totalReport);
        return Result.success("更新成功");
    }
    @RequestMapping("/insertTotalReport")
    public Result<String> insertTotalReport(@RequestBody TotalReport totalReport) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        totalReport.setUserId(userId);
        totalReportService.insertTotalReport(totalReport);
        return Result.success("插入成功");
    }

    @RequestMapping("/deleteTotalReport")
    public Result<String> deleteTotalReport() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        totalReportService.deleteTotalReportByUserId(userId);
        return Result.success("删除成功");
    }
}
