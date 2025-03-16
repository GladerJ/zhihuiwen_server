package top.mygld.zhihuiwen_server.controller;

import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.pojo.Response;
import top.mygld.zhihuiwen_server.service.ResponseService;
import top.mygld.zhihuiwen_server.service.impl.QuestionnaireService;

import java.util.Date;

@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private ResponseService responseService;

    /**
     * 提交问卷回复
     *
     * @param response 回复对象
     * @param request  HTTP请求
     * @return 保存结果
     */
    @PostMapping("/fillQuestionnaire")
    public Result<String> submitResponse(@RequestBody Response response, HttpServletRequest request) {
        if (response == null) return Result.error("参数错误");
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null && !(((String) principal).equals("anonymousUser"))) {
            Long userId = (Long) principal;
            response.setUserId(userId);
        }

        Response savedResponse = responseService.saveResponse(response, request);
        if (savedResponse != null) return Result.success("保存成功");
        return Result.error("保存失败");
    }


    // 分页查询当前用户在指定分类下的所有问卷
    @RequestMapping("/selectAll")
    public Result<PageInfo<Questionnaire>> selectAll(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam Long categoryId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(questionnaireService.selectAllById(userId, categoryId, pageNum, pageSize));
    }

    // 根据问卷ID显示问卷，供调查者填写，这里不需要token
    @GetMapping("/showQuestionnaireForEveryone/{id}")
    public Result<Questionnaire> showQuestionnaireForEveryone(@PathVariable Long id) {
        Questionnaire questionnaire = questionnaireService.selectQuestionnaireById(id);
        if (questionnaire == null) {
            return Result.error("问卷不存在");
        }
        if ((questionnaire.getStatus()).equals("draft"))
            return Result.error("问卷未发布");
        if (questionnaire.getStartTime() != null && questionnaire.getStartTime().after(new Date()))
            return Result.error("问卷未开始");
        if (questionnaire.getEndTime() != null && questionnaire.getEndTime().before(new Date()))
            return Result.error("问卷已结束");
        return Result.success(questionnaire);
    }


    // 根据问卷ID和当前用户查询问卷详情（包含题目和选项）
    @GetMapping("/editQuestionnaire/{id}")
    public Result<Questionnaire> getQuestionnaireById(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Questionnaire questionnaire = questionnaireService.selectQuestionnaireByIdAndUserId(id, userId);
        if (questionnaire == null)
            return Result.error("问卷不存在");
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
