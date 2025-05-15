package top.mygld.zhihuiwen_server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mygld.zhihuiwen_server.mapper.ReportMapper;
import top.mygld.zhihuiwen_server.pojo.Report;
import top.mygld.zhihuiwen_server.service.ReportService;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;
    @Override
    public int insertReport(Report report) {
        return reportMapper.insertReport(report);
    }

    @Override
    public int updateReport(Report report) {
        return reportMapper.updateReport(report);
    }

    @Override
    public Report selectReportById(Long id) {
        return reportMapper.selectReportById(id);
    }

    @Override
    public int selectReportIdByQuestionnaireId(Long questionnaireId) {
        return reportMapper.selectReportIdByQuestionnaireId(questionnaireId);
    }

    @Override
    public Report selectReportByQuestionnaireId(Long questionnaireId) {
        return reportMapper.selectReportByQuestionnaireId(questionnaireId);
    }

    @Override
    public int deleteReportByQuestionnaireId(Long questionnaireId) {
        return reportMapper.deleteReportByQuestionnaireId(questionnaireId);
    }

    @Override
    public String selectSummaryByQuestionnaireId(Long questionnaireId) {
        return reportMapper.selectSummaryByQuestionnaireId(questionnaireId);
    }


}
