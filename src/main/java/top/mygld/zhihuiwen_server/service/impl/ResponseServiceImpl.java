package top.mygld.zhihuiwen_server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mygld.zhihuiwen_server.pojo.Response;
import top.mygld.zhihuiwen_server.service.ResponseService;
import top.mygld.zhihuiwen_server.service.RedisService;
import top.mygld.zhihuiwen_server.mapper.AnswerMapper;
import top.mygld.zhihuiwen_server.mapper.ResponseMapper;
import top.mygld.zhihuiwen_server.pojo.Answer;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ResponseServiceImpl implements ResponseService {

    @Autowired
    private ResponseMapper responseMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private RedisService redisService;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "questionnaire", key = "#response.questionnaireId")
    })
    public Response saveResponse(Response response, HttpServletRequest request) {
        // 获取客户端IP
        String ip = getClientIp(request);
        String key = "response:ip:" + ip;
        // 限制30秒内只能提交一次
        if (Boolean.TRUE.equals(redisService.hasKey(key))) {
            throw new IllegalStateException("请勿频繁提交，请稍后再试");
        }
        // 缓存IP，30秒后过期
        redisService.setValueWithExpiry(key, true, 30, TimeUnit.SECONDS);
        // 设置IP地址和UserAgent
        response.setIpAddress(ip);
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
        redisService.deleteByPrefix("questionnaire-detail::" + response.getQuestionnaireId() + "_");
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
        responses.forEach(response -> loadAnswers(response));
        return responses;
    }

    @Override
    public int deleteResponseByQuestionnaireId(Long questionnaireId) {
        responseMapper.selectAllResponsesByQuestionnaireId(questionnaireId).forEach(resp -> {
            answerMapper.deleteAnswerByResponseId(resp.getId());
        });
        return responseMapper.deleteResponseByQuestionnaireId(questionnaireId);
    }

    @Override
    public List<Response> selectAllNeedDeleteResponsesByQuestionnaireId(Long id) {
        List<Response> responses = responseMapper.selectAllNeedDeleteResponsesByQuestionnaireId(id);
        responses.forEach(response -> loadAnswers(response));
        return responses;
    }

    @Override
    public List<Response> selectAllNotNeedDeleteResponsesByQuestionnaireId(Long id) {
        List<Response> responses = responseMapper.selectAllNotNeedDeleteResponsesByQuestionnaireId(id);
        responses.forEach(response -> loadAnswers(response));
        return responses;
    }

    @Override
    @Transactional
    public int deleteResponseByResponseId(Long responseId,Long questionnaireId) {
        redisService.deleteByPrefix("questionnaire-detail::" + questionnaireId + "_");
        // 删除关联的答案
        answerMapper.deleteAnswerByResponseId(responseId);
        // 删除响应
        return responseMapper.deleteResponseByResponseId(responseId);
    }

    @Override
    public int updateResponseValid1(Long responseId) {
        return responseMapper.updateResponseValid1(responseId);
    }

    @Override
    public int updateResponseValid0(Long responseId) {
        return responseMapper.updateResponseValid0(responseId);
    }

    @Override
    public PageInfo<Response> selectAllNotNeedDeleteResponsesByQuestionnaireId(int pageNum, int pageSize, Long questionnaireId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Response> responses = selectAllNotNeedDeleteResponsesByQuestionnaireId(questionnaireId);
        return new PageInfo<>(responses);
    }

    @Override
    public PageInfo<Response> selectAllNeedDeleteResponsesByQuestionnaireId(int pageNum, int pageSize, Long questionnaireId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Response> responses = selectAllNeedDeleteResponsesByQuestionnaireId(questionnaireId);
        return new PageInfo<>(responses);
    }

    /**
     * 加载并处理答案列表
     */
    private void loadAnswers(Response response) {
        List<Answer> answers = answerMapper.selectAllAnswersByResponseId(response.getId());
        answers.forEach(answer -> {
            if ("text".equals(answer.getAnswerType())) {
                String content = (String) answer.getAnswerContent();
                answer.setAnswerContent(content.substring(1, content.length() - 1));
            }
        });
        response.setAnswers(answers);
    }

    /**
     * 获取客户端真实IP地址
     *
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
