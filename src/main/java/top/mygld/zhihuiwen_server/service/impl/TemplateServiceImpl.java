package top.mygld.zhihuiwen_server.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mygld.zhihuiwen_server.mapper.TemplateMapper;
import top.mygld.zhihuiwen_server.pojo.*;
import top.mygld.zhihuiwen_server.service.ReportService;
import top.mygld.zhihuiwen_server.service.ResponseService;
import top.mygld.zhihuiwen_server.service.TemplateService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Override
    public PageInfo<Template> selectAllById(Long userId, Long categoryId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(templateMapper.selectAllById(userId, categoryId));
    }

    @Override
    public int deleteTemplate(Template template) {
        // 先删除级联的选项和题目，再删除问卷主记录
        templateMapper.deleteOptionsByTemplateId(template.getId());
        templateMapper.deleteQuestionsByTemplateId(template.getId());
        return templateMapper.deleteTemplate(template.getId(), template.getUserId());
    }

    @Override
    public PageInfo<Template> selectTemplateLike(Long userId, Long categoryId, String content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(templateMapper.selectTemplateLike(userId, categoryId, content));
    }

    @Override
    @Transactional
    public int insertTemplate(Template template) {
        int result = templateMapper.insertTemplate(template);
        if (template.getQuestions() != null) {
            for (TemplateQuestion question : template.getQuestions()) {
                // 设置问卷ID
                question.setTemplateId(template.getId());
                templateMapper.insertTemplateQuestion(question);
                if (question.getOptions() != null) {
                    for (TemplateOption option : question.getOptions()) {
                        option.setQuestionId(question.getId());
                        templateMapper.insertTemplateOption(option);
                    }
                }
            }
        }
        return result;
    }

    @Override
    @Transactional
    public int updateTemplate(Template template) {
        deleteTemplate(template);
        return insertTemplate(template);
    }

    @Override
    public Template selectTemplateByIdAndUserId(Long id, Long userId) {
        return templateMapper.selectTemplateByIdAndUserId(id, userId);
    }

    @Override
    public Template selectTemplateById(Long id) {
        return templateMapper.selectTemplateById(id);
    }

    @Override
    public boolean checkTemplateForUserId(Long userId, Long templateId) {
        return templateMapper.selectTemplateByIdAndUserId(templateId, userId) != null;
    }

    @Override
    public int addUsageCount(Long templateId) {
        return templateMapper.addUsageCount(templateId);
    }

    @Override
    public PageInfo<Template> selectAllPublicTemplates(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(templateMapper.selectAllPublicTemplates());
    }

    @Override
    public PageInfo<Template> selectAllPublicTemplatesLike(String content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(templateMapper.selectAllPublicTemplatesLike(content));
    }

    @Override
    public Template selectPublicTemplateById(Long id) {
        return templateMapper.selectPublicTemplateById(id);
    }

    @Override
    public List<Template> selectAllTemplatesByUserId(Long userId) {
        List<Template> questionnaires = templateMapper.selectAllTemplatesByUserId(userId);
        List<Template> results = new ArrayList<>();
        questionnaires.stream().forEach(template -> {
            template = selectTemplateById(template.getId());
            results.add(template);
        });
        return results;
    }

}
