package top.mygld.zhihuiwen_server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mygld.zhihuiwen_server.mapper.TotalReportMapper;
import top.mygld.zhihuiwen_server.pojo.TotalReport;
import top.mygld.zhihuiwen_server.service.TotalReportService;

@Service
public class TotalReportServiceImpl implements TotalReportService {
    @Autowired
    private TotalReportMapper totalReportMapper;
    @Override
    public int insertTotalReport(TotalReport TotalReport) {
        return totalReportMapper.insertTotalReport(TotalReport);
    }

    @Override
    public int updateTotalReport(TotalReport TotalReport) {
        return totalReportMapper.updateTotalReport(TotalReport);
    }

    @Override
    public int deleteTotalReportByUserId(Long userId) {
        return totalReportMapper.deleteTotalReportByUserId(userId);
    }

    @Override
    public TotalReport selectTotalReportByUserId(Long userId) {
        return totalReportMapper.selectTotalReportByUserId(userId);
    }
}
