package top.mygld.zhihuiwen_server.service;

import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import top.mygld.zhihuiwen_server.pojo.Response;

import java.util.List;

public interface ResponseService {
    @Transactional
    Response saveResponse(Response response, HttpServletRequest request);

    @Transactional
    PageInfo<Response> selectAllResponsesByQuestionnaireId(int pageNum, int pageSize,Long questionnaireId);

    @Transactional
    List<Response> selectAllResponsesByQuestionnaireId(Long id);

    @Transactional
    int deleteResponseByQuestionnaireId(Long questionnaireId);
    @Transactional
    PageInfo<Response> selectAllNeedDeleteResponsesByQuestionnaireId(int pageNum, int pageSize, Long questionnaireId);

    @Transactional
    List<Response> selectAllNeedDeleteResponsesByQuestionnaireId(Long id);

    @Transactional
    int updateResponseValid1(Long responseId);

    @Transactional
    int updateResponseValid0(Long responseId);

    @Transactional
    PageInfo<Response> selectAllNotNeedDeleteResponsesByQuestionnaireId(int pageNum, int pageSize, Long questionnaireId);

    @Transactional
    public List<Response> selectAllNotNeedDeleteResponsesByQuestionnaireId(Long id);


    int deleteResponseByResponseId(Long responseId,Long questionnaireId);
}
