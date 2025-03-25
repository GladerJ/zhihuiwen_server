package top.mygld.zhihuiwen_server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mygld.zhihuiwen_server.mapper.QuestionnaireMapper;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireQuestion;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireOption;
import top.mygld.zhihuiwen_server.service.impl.QuestionnaireService;

@Service
public class QuestionnaireServiceImpl implements QuestionnaireService {

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

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
        // 更新问卷主记录
        int result = questionnaireMapper.updateQuestionnaire(questionnaire);
        // 删除原有的题目和选项（级联删除）
        questionnaireMapper.deleteOptionsByQuestionnaireId(questionnaire.getId());
        questionnaireMapper.deleteQuestionsByQuestionnaireId(questionnaire.getId());
        // 重新插入新的题目和选项
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
        return result;
    }

    @Override
    public Questionnaire selectQuestionnaireByIdAndUserId(Long id, Long userId) {
        return questionnaireMapper.selectQuestionnaireByIdAndUserId(id, userId);
    }

    @Override
    public Questionnaire selectQuestionnaireById(Long id) {
        return questionnaireMapper.selectQuestionnaireById(id);
    }
}
