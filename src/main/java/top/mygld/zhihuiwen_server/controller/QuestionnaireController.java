package top.mygld.zhihuiwen_server.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.pojo.*;
import top.mygld.zhihuiwen_server.service.ReportService;
import top.mygld.zhihuiwen_server.service.ResponseService;
import top.mygld.zhihuiwen_server.service.QuestionnaireService;

import java.security.PrivilegedAction;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private ReportService reportService;
    //返回AI生成报告信息
    @GetMapping("/getReport/{id}")
    public Result<Report> getReport(@PathVariable Long id){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = questionnaireService.checkQuestionnaireForUserId(userId,id);
        if(!flag) return Result.error("无权限");
        Report report = reportService.selectReportByQuestionnaireId(id);
        return Result.success(report);
    }

    // 分页查询问卷填写信息
    @RequestMapping("/selectAllResponses")
    public Result<PageInfo<Response>> selectAllResponses(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam Long questionnaireId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = questionnaireService.checkQuestionnaireForUserId(userId,questionnaireId);
        if(!flag) return Result.error("无权限");
        return Result.success(responseService.selectAllResponsesByQuestionnaireId(pageNum,pageSize,questionnaireId));
    }


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


    // 根据问卷ID和当前用户查询问卷详情（包含题目和选项）以及每个选项的具体填写情况，供调查者查看
    @GetMapping("/analyzeQuestionnaire/{id}")
    public Result<Questionnaire> analyzeQuestionnaireById(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = questionnaireService.checkQuestionnaireForUserId(userId,id);
        if(!flag) return Result.error("无权限");
        Questionnaire questionnaire = questionnaireService.selectQuestionnaireByIdDetail(id,userId);
        if (questionnaire == null)
            return Result.error("问卷不存在");
        return Result.success(questionnaire);
    }



    @RequestMapping("/selectAllNeedDeleteResponses")
    public Result<PageInfo<Response>> selectAllNeedDeleteResponses(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam Long questionnaireId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = questionnaireService.checkQuestionnaireForUserId(userId,questionnaireId);
        if(!flag) return Result.error("无权限");
        return Result.success(responseService.selectAllNeedDeleteResponsesByQuestionnaireId(pageNum,pageSize,questionnaireId));
    }

    @RequestMapping("/selectAllNotNeedDeleteResponses")
    public Result<PageInfo<Response>> selectAllNotNeedDeleteResponses(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam Long questionnaireId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = questionnaireService.checkQuestionnaireForUserId(userId,questionnaireId);
        if(!flag) return Result.error("无权限");
        return Result.success(responseService.selectAllNotNeedDeleteResponsesByQuestionnaireId(pageNum,pageSize,questionnaireId));
    }


    @RequestMapping("/updateResponseValid1")
    public Result<String> updateResponseValid1(@RequestParam Long questionnaireId,@RequestParam Long responseId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = questionnaireService.checkQuestionnaireForUserId(userId,questionnaireId);
        if(!flag) return Result.error("无权限");
        responseService.updateResponseValid1(responseId);
        return Result.success("修改成功！");
    }

    @RequestMapping("/updateResponseValid0")
    public Result<String> updateResponseValid0(@RequestParam Long questionnaireId,@RequestParam Long responseId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = questionnaireService.checkQuestionnaireForUserId(userId,questionnaireId);
        if(!flag) return Result.error("无权限");
        responseService.updateResponseValid0(responseId);
        return Result.success("修改成功！");
    }

    @GetMapping("/deleteReponse")
    public Result<String> deleteResponse(Long questionnaireId,Long responseId){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = questionnaireService.checkQuestionnaireForUserId(userId,questionnaireId);
        if(!flag) return Result.error("无权限");
        responseService.deleteResponseByResponseId(responseId,questionnaireId);
        return Result.success("删除成功");
    }

}
