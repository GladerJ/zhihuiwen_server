package top.mygld.zhihuiwen_server.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
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
    public int deleteQuestionnaire(Questionnaire questionnaire) {
        // 先删除级联的选项和题目，再删除问卷主记录
        questionnaireMapper.deleteOptionsByQuestionnaireId(questionnaire.getId());
        questionnaireMapper.deleteQuestionsByQuestionnaireId(questionnaire.getId());
        //删除问卷的所有回复和答案以及报告
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
    public int updateQuestionnaire(Questionnaire questionnaire) {
        deleteQuestionnaire(questionnaire);
        return insertQuestionnaire(questionnaire);
    }

    @Override
    public Questionnaire selectQuestionnaireByIdAndUserId(Long id, Long userId) {
        return questionnaireMapper.selectQuestionnaireByIdAndUserId(id, userId);
    }

    @Override
    public Questionnaire selectQuestionnaireById(Long id) {
        return questionnaireMapper.selectQuestionnaireById(id);
    }

    @Override
    public boolean checkQuestionnaireForUserId(Long userId, Long questionnaireId) {
        return questionnaireMapper.selectQuestionnaireByIdAndUserId(questionnaireId, userId) != null;
    }

    @Override
    public Questionnaire selectQuestionnaireByIdDetail(Long id,Long userId) {
        Questionnaire questionnaire = selectQuestionnaireByIdAndUserId(id, userId);
        List<Response> responses = responseService.selectAllResponsesByQuestionnaireId(id);
        Map<Long, Long> map = new HashMap<>();
        responses.stream().forEach(response -> {
            response.getAnswers().stream().forEach(answer -> {
                if (answer.getAnswerType().equals("option")) {
                    Object content = answer.getAnswerContent();

                    Long[] ids;
                    if (content instanceof String) {
                        // 如果是 JSON 格式的字符串，使用 FastJSON 解析
                        ids = JSON.parseObject((String) content, Long[].class);
                    } else if (content instanceof List) {
                        // 如果数据库返回的是 List，则转换
                        List<?> list = (List<?>) content;
                        ids = list.toArray(new Long[0]);
                    } else {
                        throw new IllegalArgumentException("未知的 answerContent 类型: " + content.getClass().getName());
                    }

                    // 统计选项数量
                    for (Long optionId : ids) {
                        map.put(optionId, map.getOrDefault(optionId, 0L) + 1);
                    }
                }
            });
        });



        List<QuestionnaireQuestion> questions = questionnaire.getQuestions();
        questions.stream().forEach(question -> {
            List<QuestionnaireOption> options = question.getOptions();
            options.stream().forEach(option -> {
                if(map.containsKey(option.getId())){
                    option.setCount(map.get(option.getId()));
                }else{
                    option.setCount(0L);
                }
            });
            question.setOptions(options);
        });
        questionnaire.setQuestions(questions);
        return questionnaire;
    }

    @Override
    public List<Questionnaire> selectAllQuestionnairesByUserId(Long userId) {
        List<Questionnaire> questionnaires = questionnaireMapper.selectAllQuestionnairesByUserId(userId);
        List<Questionnaire> results = new ArrayList<>();
        questionnaires.stream().forEach(questionnaire -> {
            questionnaire = selectQuestionnaireByIdDetail(questionnaire.getId(),userId);
            results.add(questionnaire);
        });
        return results;
    }
}
