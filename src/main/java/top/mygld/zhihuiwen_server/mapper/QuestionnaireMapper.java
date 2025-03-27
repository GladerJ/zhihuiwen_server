package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireQuestion;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireOption;
import java.util.List;

@Mapper
public interface QuestionnaireMapper {

    /**
     * 新增问卷
     * @param questionnaire 问卷实体对象
     * @return 受影响的行数
     */
    int insertQuestionnaire(Questionnaire questionnaire);

    /**
     * 新增问卷题目
     * @param question 问卷题目实体对象
     * @return 受影响的行数
     */
    int insertQuestionnaireQuestion(QuestionnaireQuestion question);

    /**
     * 新增问卷选项
     * @param option 问卷选项实体对象
     * @return 受影响的行数
     */
    int insertQuestionnaireOption(QuestionnaireOption option);

    /**
     * 根据问卷ID和用户ID删除问卷
     * @param id 问卷ID
     * @param userId 用户ID
     * @return 受影响的行数
     */
    int deleteQuestionnaire(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据问卷ID删除所有问卷题目
     * @param questionnaireId 问卷ID
     * @return 受影响的行数
     */
    int deleteQuestionsByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    /**
     * 根据问卷ID删除所有问卷选项（级联删除）
     * @param questionnaireId 问卷ID
     * @return 受影响的行数
     */
    int deleteOptionsByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    /**
     * 根据问卷ID和用户ID更新问卷
     * @param questionnaire 问卷实体对象（必须包含 id 与 userId）
     * @return 受影响的行数
     */
    int updateQuestionnaire(Questionnaire questionnaire);

    /**
     * 根据问卷ID和用户ID查询问卷（包括题目和选项）
     * @param id 问卷ID
     * @param userId 用户ID
     * @return 问卷实体对象
     */
    Questionnaire selectQuestionnaireByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据用户ID和分类ID查询所有问卷
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @return 问卷列表
     */
    List<Questionnaire> selectAllById(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /**
     * 根据用户ID、分类ID及问卷标题模糊查询问卷
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @param title 标题关键字
     * @return 问卷列表
     */
    List<Questionnaire> selectQuestionnaireLike(@Param("userId") Long userId,
                                                @Param("categoryId") Long categoryId,
                                                @Param("title") String title);

    /**
     * 将问卷过期的改一下
     */
    int updateQuestionnaireStatus();

    Questionnaire selectQuestionnaireById(@Param("id") Long id);

}
