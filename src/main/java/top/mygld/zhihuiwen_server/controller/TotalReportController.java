package top.mygld.zhihuiwen_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mygld.zhihuiwen_server.pojo.TotalReport;
import top.mygld.zhihuiwen_server.service.ReportService;
import top.mygld.zhihuiwen_server.service.TotalReportService;

@RestController
@RequestMapping("/totalReport")
public class TotalReportController {
    @Autowired
    private TotalReportService totalReportService;
    @RequestMapping("/getTotalReport")
    public TotalReport getTotalReport() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return totalReportService.selectTotalReportByUserId(userId);
    }
    @RequestMapping("/updateTotalReport")
    public int updateTotalReport(@RequestBody TotalReport totalReport) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        totalReport.setUserId(userId);
        return totalReportService.updateTotalReport(totalReport);
    }
    @RequestMapping("/insertTotalReport")
    public int insertTotalReport(@RequestBody TotalReport totalReport) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        totalReport.setUserId(userId);
        return totalReportService.insertTotalReport(totalReport);
    }

    @RequestMapping("/deleteTotalReport")
    public int deleteTotalReport() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return totalReportService.deleteTotalReportByUserId(userId);
    }
}
