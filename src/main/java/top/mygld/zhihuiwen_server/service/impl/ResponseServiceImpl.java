package top.mygld.zhihuiwen_server.service.impl;

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
