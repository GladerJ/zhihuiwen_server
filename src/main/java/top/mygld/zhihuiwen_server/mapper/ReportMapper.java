package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Report;

@Mapper
public interface ReportMapper {
    int insertReport(Report report);
    int updateReport(Report report);
    Report selectReportById(Long id);
    int selectReportIdByQuestionnaireId(Long questionnaireId);
    Report selectReportByQuestionnaireId(Long questionnaireId);
    int deleteReportByQuestionnaireId(Long questionnaireId);
}
