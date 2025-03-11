package top.mygld.zhihuiwen_server.controller;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.service.impl.QuestionnaireService;

@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    // 分页查询当前用户在指定分类下的所有问卷
    @RequestMapping("/selectAll")
    public Result<PageInfo<Questionnaire>> selectAll(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam Long categoryId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(questionnaireService.selectAllById(userId, categoryId, pageNum, pageSize));
    }

    // 根据问卷ID和当前用户查询问卷详情（包含题目和选项）
    @GetMapping("/editQuestionnaire/{id}")
    public Result<Questionnaire> getQuestionnaireById(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Questionnaire questionnaire = questionnaireService.selectQuestionnaireById(id, userId);
        return Result.success(questionnaire);
    }

    // 新增问卷接口，支持级联插入题目和选项
    @PostMapping("/addQuestionnaire")
    public Result<Questionnaire> insertQuestionnaire(@RequestBody Questionnaire questionnaire) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        questionnaire.setUserId(userId);
        questionnaireService.insertQuestionnaire(questionnaire);
        return Result.success(questionnaire);
    }

    // 更新问卷接口：先删除原有的题目与选项，再重新添加新的数据
    @PostMapping("/updateQuestionnaire")
    public Result<Questionnaire> updateQuestionnaire(@RequestBody Questionnaire questionnaire) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        questionnaire.setUserId(userId);
        questionnaireService.updateQuestionnaire(questionnaire);
        return Result.success(questionnaire);
    }

    // 删除问卷接口，级联删除题目与选项
    @PostMapping("/deleteQuestionnaire")
    public Result<String> deleteQuestionnaire(@RequestBody Questionnaire questionnaire) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        questionnaire.setUserId(userId);
        questionnaireService.deleteQuestionnaire(questionnaire);
        return Result.success("删除成功");
    }

    // 模糊搜索接口，查询当前用户在指定分类下标题包含关键字的问卷
    @RequestMapping("/selectQuestionnaireLike")
    public Result<PageInfo<Questionnaire>> selectQuestionnaireLike(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam Long categoryId,
            @RequestParam String content) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(questionnaireService.selectQuestionnaireLike(userId, categoryId, content, pageNum, pageSize));
    }
}
