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
import top.mygld.zhihuiwen_server.service.TemplateService;

import java.security.PrivilegedAction;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping("/selectPublicTemplateById/{id}")
    public Result<Template> selectPublicTemplateById(@PathVariable Long id) {
        Template template = templateService.selectPublicTemplateById(id);
        if (template == null)
            return Result.error("问卷不存在");
        return Result.success(template);
    }

    @GetMapping("/selectAllPublicTemplates")
    public Result<PageInfo<Template>> selectAllPublicTemplates(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize
    ){
        return Result.success(templateService.selectAllPublicTemplates(pageNum, pageSize));
    }

    @GetMapping("/selectAllPublicTemplatesLike")
    public Result<PageInfo<Template>> selectAllPublicTemplatesLike(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam String content
    ){
        return Result.success(templateService.selectAllPublicTemplatesLike(content, pageNum, pageSize));
    }

    @GetMapping("/addUsageCount")
    public Result<String> addUsageCount(@RequestParam Long templateId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!templateService.checkTemplateForUserId(userId, templateId)){
            templateService.addUsageCount(templateId);
            return Result.success("添加成功");
        }
        return Result.success("响应成功");
    }


    @RequestMapping("/selectAll")
    public Result<PageInfo<Template>> selectAll(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam Long categoryId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(templateService.selectAllById(userId, categoryId, pageNum, pageSize));
    }


    // 根据问卷ID和当前用户查询问卷详情（包含题目和选项）
    @GetMapping("/editTemplate/{id}")
    public Result<Template> getTemplateById(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Template template = templateService.selectTemplateByIdAndUserId(id, userId);
        if (template == null)
            return Result.error("问卷不存在");
        return Result.success(template);
    }

    // 新增问卷接口，支持级联插入题目和选项
    @PostMapping("/addTemplate")
    public Result<Template> insertTemplate(@RequestBody Template template) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        template.setUserId(userId);
        templateService.insertTemplate(template);
        return Result.success(template);
    }

    // 更新问卷接口：先删除原有的题目与选项，再重新添加新的数据
    @PostMapping("/updateTemplate")
    public Result<Template> updateTemplate(@RequestBody Template template) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        template.setUserId(userId);
        templateService.updateTemplate(template);
        return Result.success(template);
    }

    // 删除问卷接口，级联删除题目与选项
    @PostMapping("/deleteTemplate")
    public Result<String> deleteTemplate(@RequestBody Template template) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        template.setUserId(userId);
        templateService.deleteTemplate(template);
        return Result.success("删除成功");
    }

    // 模糊搜索接口，查询当前用户在指定分类下标题包含关键字的问卷
    @RequestMapping("/selectTemplateLike")
    public Result<PageInfo<Template>> selectTemplateLike(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam Long categoryId,
            @RequestParam String content) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(templateService.selectTemplateLike(userId, categoryId, content, pageNum, pageSize));
    }

}
