package top.mygld.zhihuiwen_server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mygld.zhihuiwen_server.pojo.Response;
import top.mygld.zhihuiwen_server.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import top.mygld.zhihuiwen_server.mapper.AnswerMapper;
import top.mygld.zhihuiwen_server.mapper.ResponseMapper;
import top.mygld.zhihuiwen_server.pojo.Answer;


import java.util.Date;
import java.util.List;

@Service
public class ResponseServiceImpl implements ResponseService{

    @Autowired
    private ResponseMapper responseMapper;

    @Autowired
    private AnswerMapper answerMapper;
    @Override
    @Transactional
    public Response saveResponse(Response response, HttpServletRequest request) {

        // 设置IP地址和UserAgent
        response.setIpAddress(getClientIp(request));
        response.setUserAgent(request.getHeader("User-Agent"));

        // 设置提交时间
        if (response.getSubmittedAt() == null) {
            response.setSubmittedAt(new Date());
        }

        // 保存回复
        responseMapper.insertResponse(response);

        // 保存所有答案
        List<Answer> answers = response.getAnswers();
        if (answers != null && !answers.isEmpty()) {
            for (Answer answer : answers) {
                // 设置responseId关联
                answer.setResponseId(response.getId());
                answerMapper.insertAnswer(answer);
            }
        }

        return response;
    }

    @Override
    public PageInfo<Response> selectAllResponsesByQuestionnaireId(int pageNum, int pageSize, Long questionnaireId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Response> responses = selectAllResponsesByQuestionnaireId(questionnaireId);
        return new PageInfo<>(responses);
    }

    @Override
    public List<Response> selectAllResponsesByQuestionnaireId(Long id) {
        List<Response> responses = responseMapper.selectAllResponsesByQuestionnaireId(id);
        responses.stream().forEach(response -> {
            List<Answer> answers = answerMapper.selectAllAnswersByResponseId(response.getId());
            answers.stream().forEach(answer -> {
                if(answer.getAnswerType().equals("text")){
                    String content = (String)answer.getAnswerContent();
                    answer.setAnswerContent(content.substring(1,content.length()-1));
                }
            });
            response.setAnswers(answers);
        });
        return responses;
    }

    @Override
    public int deleteResponseByQuestionnaireId(Long questionnaireId) {
        responseMapper.selectAllResponsesByQuestionnaireId(questionnaireId).stream().forEach(response -> {
            answerMapper.deleteAnswerByResponseId(response.getId());
        });
        return responseMapper.deleteResponseByQuestionnaireId(questionnaireId);
    }

    /**
     * 获取客户端真实IP地址
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 多个代理的情况，第一个IP为客户端真实IP
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }
}
