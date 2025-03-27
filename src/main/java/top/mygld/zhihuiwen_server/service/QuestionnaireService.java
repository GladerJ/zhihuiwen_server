package top.mygld.zhihuiwen_server.service;

import com.github.pagehelper.PageInfo;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;

public interface QuestionnaireService {

    /**
     * 根据用户ID和分类ID分页查询问卷列表
     */
    PageInfo<Questionnaire> selectAllById(Long userId, Long categoryId, int pageNum, int pageSize);

    /**
     * 根据问卷对象删除问卷（级联删除问卷下的题目与选项）
     */
    int deleteQuestionnaire(Questionnaire questionnaire);

    /**
     * 根据用户ID、分类ID及标题关键字分页模糊查询问卷列表
     */
    PageInfo<Questionnaire> selectQuestionnaireLike(Long userId, Long categoryId, String content, int pageNum, int pageSize);

    /**
     * 新增问卷（级联插入题目与选项）
     */
    int insertQuestionnaire(Questionnaire questionnaire);

    /**
     * 更新问卷（修改时先删除级联的题目和选项，再重新插入）
     */
    int updateQuestionnaire(Questionnaire questionnaire);

    /**
     * 根据问卷ID和用户ID查询问卷详情（包含题目和选项）
     */
    Questionnaire selectQuestionnaireByIdAndUserId(Long id, Long userId);
    Questionnaire selectQuestionnaireById(Long id);

    boolean checkQuestionnaireForUserId(Long userId, Long questionnaireId);

    Questionnaire selectQuestionnaireByIdDetail(Long id,Long userId);
}
