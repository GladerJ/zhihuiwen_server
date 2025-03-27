package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Report;
import top.mygld.zhihuiwen_server.pojo.TotalReport;

@Mapper
public interface TotalReportMapper {
    int insertTotalReport(TotalReport TotalReport);
    int updateTotalReport(TotalReport TotalReport);
    int deleteTotalReportByUserId(Long userId);
    TotalReport selectTotalReportByUserId(Long userId);
}
