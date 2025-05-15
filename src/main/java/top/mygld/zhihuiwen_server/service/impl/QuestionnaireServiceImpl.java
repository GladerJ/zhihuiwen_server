package top.mygld.zhihuiwen_server.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mygld.zhihuiwen_server.mapper.QuestionnaireMapper;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireQuestion;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireOption;
import top.mygld.zhihuiwen_server.pojo.Response;
import top.mygld.zhihuiwen_server.service.ReportService;
import top.mygld.zhihuiwen_server.service.ResponseService;
import top.mygld.zhihuiwen_server.service.QuestionnaireService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionnaireServiceImpl implements QuestionnaireService {

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private ReportService reportService;

    @Override
    public PageInfo<Questionnaire> selectAllById(Long userId, Long categoryId, int pageNum, int pageSize) {
        questionnaireMapper.updateQuestionnaireStatus();
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(questionnaireMapper.selectAllById(userId, categoryId));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "questionnaire", key = "#questionnaire.id"),
            @CacheEvict(value = "questionnaire", key = "#questionnaire.id + '_' + #questionnaire.userId"),
            @CacheEvict(value = "questionnaire-detail", key = "#questionnaire.id + '_' + #questionnaire.userId"),
            @CacheEvict(value = "questionnaire-list", key = "'user_' + #questionnaire.userId"),
            @CacheEvict(value = "questionnaire-all", key = "'user_' + #questionnaire.userId")
    })
    public int deleteQuestionnaire(Questionnaire questionnaire) {
        // 先删除级联的选项和题目，再删除问卷主记录
        questionnaireMapper.deleteOptionsByQuestionnaireId(questionnaire.getId());
        questionnaireMapper.deleteQuestionsByQuestionnaireId(questionnaire.getId());
        // 删除问卷的所有回复和答案以及报告
        reportService.deleteReportByQuestionnaireId(questionnaire.getId());
        responseService.deleteResponseByQuestionnaireId(questionnaire.getId());
        return questionnaireMapper.deleteQuestionnaire(questionnaire.getId(), questionnaire.getUserId());
    }

    @Override
    public PageInfo<Questionnaire> selectQuestionnaireLike(Long userId, Long categoryId, String content, int pageNum, int pageSize) {
        questionnaireMapper.updateQuestionnaireStatus();
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(questionnaireMapper.selectQuestionnaireLike(userId, categoryId, content));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "questionnaire-list", key = "'user_' + #questionnaire.userId"),
            @CacheEvict(value = "questionnaire-all", key = "'user_' + #questionnaire.userId")
    })
    public int insertQuestionnaire(Questionnaire questionnaire) {
        int result = questionnaireMapper.insertQuestionnaire(questionnaire);
        if (questionnaire.getQuestions() != null) {
            for (QuestionnaireQuestion question : questionnaire.getQuestions()) {
                // 设置问卷ID
                question.setQuestionnaireId(questionnaire.getId());
                questionnaireMapper.insertQuestionnaireQuestion(question);
                if (question.getOptions() != null) {
                    for (QuestionnaireOption option : question.getOptions()) {
                        option.setQuestionId(question.getId());
                        questionnaireMapper.insertQuestionnaireOption(option);
                    }
                }
            }
        }
        return result;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "questionnaire", key = "#questionnaire.id"),
            @CacheEvict(value = "questionnaire", key = "#questionnaire.id + '_' + #questionnaire.userId"),
            @CacheEvict(value = "questionnaire-detail", key = "#questionnaire.id + '_' + #questionnaire.userId"),
            @CacheEvict(value = "questionnaire-list", key = "'user_' + #questionnaire.userId"),
            @CacheEvict(value = "questionnaire-all", key = "'user_' + #questionnaire.userId")
    })
    public int updateQuestionnaire(Questionnaire questionnaire) {
        // 删除旧数据并重新插入，实现更新逻辑
        questionnaireMapper.deleteOptionsByQuestionnaireId(questionnaire.getId());
        questionnaireMapper.deleteQuestionsByQuestionnaireId(questionnaire.getId());
        reportService.deleteReportByQuestionnaireId(questionnaire.getId());
        responseService.deleteResponseByQuestionnaireId(questionnaire.getId());
        questionnaireMapper.updateQuestionnaire(questionnaire);
        if (questionnaire.getQuestions() != null) {
            for (QuestionnaireQuestion question : questionnaire.getQuestions()) {
                question.setQuestionnaireId(questionnaire.getId());
                questionnaireMapper.insertQuestionnaireQuestion(question);
                if (question.getOptions() != null) {
                    for (QuestionnaireOption option : question.getOptions()) {
                        option.setQuestionId(question.getId());
                        questionnaireMapper.insertQuestionnaireOption(option);
                    }
                }
            }
        }
        return 1;
    }

    @Override
    @Cacheable(value = "questionnaire", key = "#id + '_' + #userId", unless = "#result == null")
    public Questionnaire selectQuestionnaireByIdAndUserId(Long id, Long userId) {
        return questionnaireMapper.selectQuestionnaireByIdAndUserId(id, userId);
    }

    @Override
    @Cacheable(value = "questionnaire", key = "#id", unless = "#result == null")
    public Questionnaire selectQuestionnaireById(Long id) {
        return questionnaireMapper.selectQuestionnaireById(id);
    }

    @Override
    public boolean checkQuestionnaireForUserId(Long userId, Long questionnaireId) {
        return questionnaireMapper.selectQuestionnaireByIdAndUserId(questionnaireId, userId) != null;
    }

    @Override
    @Cacheable(value = "questionnaire-detail", key = "#id + '_' + #userId", unless = "#result == null")
    public Questionnaire selectQuestionnaireByIdDetail(Long id, Long userId) {
        Questionnaire questionnaire = selectQuestionnaireByIdAndUserId(id, userId);
        List<Response> responses = responseService.selectAllResponsesByQuestionnaireId(id);
        Map<Long, Long> map = new HashMap<>();
        for (Response response : responses) {
            for (var answer : response.getAnswers()) {
                if ("option".equals(answer.getAnswerType())) {
                    Long[] ids;
                    Object content = answer.getAnswerContent();
                    if (content instanceof String) {
                        ids = JSON.parseObject((String) content, Long[].class);
                    } else if (content instanceof List) {
                        List<?> list = (List<?>) content;
                        ids = list.toArray(new Long[0]);
                    } else {
                        throw new IllegalArgumentException("未知的 answerContent 类型: " + content.getClass().getName());
                    }
                    for (Long optionId : ids) {
                        map.put(optionId, map.getOrDefault(optionId, 0L) + 1);
                    }
                }
            }
        }
        for (QuestionnaireQuestion question : questionnaire.getQuestions()) {
            for (QuestionnaireOption option : question.getOptions()) {
                option.setCount(map.getOrDefault(option.getId(), 0L));
            }
        }
        return questionnaire;
    }

    @Override
    @Cacheable(value = "questionnaire-all", key = "'user_' + #userId", unless = "#result == null || #result.isEmpty()")
    public List<Questionnaire> selectAllQuestionnairesByUserId(Long userId) {
        List<Questionnaire> questionnaires = questionnaireMapper.selectAllQuestionnairesByUserId(userId);
        List<Questionnaire> results = new ArrayList<>();
        for (Questionnaire questionnaire : questionnaires) {
            results.add(selectQuestionnaireByIdDetail(questionnaire.getId(), userId));
        }
        return results;
    }

    @Override
    @Cacheable(value = "questionnaire-list", key = "'user_' + #userId", unless = "#result == null || #result.isEmpty()")
    public List<Questionnaire> selectAllQuestionnaireListByUserId(Long userId) {
        return questionnaireMapper.selectAllQuestionnairesByUserId(userId);
    }
}
