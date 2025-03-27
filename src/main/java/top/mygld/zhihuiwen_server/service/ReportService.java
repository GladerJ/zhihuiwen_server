package top.mygld.zhihuiwen_server.service;

import top.mygld.zhihuiwen_server.pojo.Report;

public interface ReportService {
    int insertReport(Report report);
    int updateReport(Report report);
    Report selectReportById(Long id);
    int selectReportIdByQuestionnaireId(Long questionnaireId);
    Report selectReportByQuestionnaireId(Long questionnaireId);
    int deleteReportByQuestionnaireId(Long questionnaireId);
}
