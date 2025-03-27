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
}
