package top.mygld.zhihuiwen_server.service;
import top.mygld.zhihuiwen_server.pojo.TotalReport;

public interface TotalReportService {
    int insertTotalReport(TotalReport TotalReport);
    int updateTotalReport(TotalReport TotalReport);
    int deleteTotalReportByUserId(Long userId);
    TotalReport selectTotalReportByUserId(Long userId);
}
