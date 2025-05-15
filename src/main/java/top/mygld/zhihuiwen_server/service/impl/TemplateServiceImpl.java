package top.mygld.zhihuiwen_server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mygld.zhihuiwen_server.mapper.TemplateMapper;
import top.mygld.zhihuiwen_server.pojo.Template;
import top.mygld.zhihuiwen_server.pojo.TemplateOption;
import top.mygld.zhihuiwen_server.pojo.TemplateQuestion;
import top.mygld.zhihuiwen_server.service.TemplateService;

import java.util.ArrayList;
import java.util.List;

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
    @Caching(evict = {
            @CacheEvict(value = "template", key = "#template.id"),
            @CacheEvict(value = "template", key = "#template.id + '_' + #template.userId"),
            @CacheEvict(value = "template-list", key = "'user_' + #template.userId"),
            @CacheEvict(value = "template-all", key = "'user_' + #template.userId"),
            @CacheEvict(value = "template-public", key = "#template.id")
    })
    public int deleteTemplate(Template template) {
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
    @Caching(evict = {
            @CacheEvict(value = "template-list", key = "'user_' + #template.userId"),
            @CacheEvict(value = "template-all", key = "'user_' + #template.userId"),
            @CacheEvict(value = "template-public", key = "#template.id")
    })
    public int insertTemplate(Template template) {
        int result = templateMapper.insertTemplate(template);
        if (template.getQuestions() != null) {
            for (TemplateQuestion question : template.getQuestions()) {
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
    @Caching(evict = {
            @CacheEvict(value = "template", key = "#template.id"),
            @CacheEvict(value = "template", key = "#template.id + '_' + #template.userId"),
            @CacheEvict(value = "template-list", key = "'user_' + #template.userId"),
            @CacheEvict(value = "template-all", key = "'user_' + #template.userId"),
            @CacheEvict(value = "template-public", key = "#template.id")
    })
    public int updateTemplate(Template template) {
        templateMapper.deleteOptionsByTemplateId(template.getId());
        templateMapper.deleteQuestionsByTemplateId(template.getId());
        templateMapper.updateTemplate(template);
        if (template.getQuestions() != null) {
            for (TemplateQuestion question : template.getQuestions()) {
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
        return 1;
    }

    @Override
    @Cacheable(value = "template", key = "#id + '_' + #userId", unless = "#result == null")
    public Template selectTemplateByIdAndUserId(Long id, Long userId) {
        return templateMapper.selectTemplateByIdAndUserId(id, userId);
    }

    @Override
    @Cacheable(value = "template", key = "#id", unless = "#result == null")
    public Template selectTemplateById(Long id) {
        return templateMapper.selectTemplateById(id);
    }

    @Override
    public boolean checkTemplateForUserId(Long userId, Long templateId) {
        return templateMapper.selectTemplateByIdAndUserId(templateId, userId) != null;
    }

    @Override
    @Cacheable(value = "template-all", key = "'user_' + #userId", unless = "#result == null || #result.isEmpty()")
    public List<Template> selectAllTemplatesByUserId(Long userId) {
        List<Template> templates = templateMapper.selectAllTemplatesByUserId(userId);
        List<Template> results = new ArrayList<>();
        for (Template template : templates) {
            results.add(selectTemplateById(template.getId()));
        }
        return results;
    }

    @Override
    @Cacheable(value = "template-list", key = "'user_' + #userId", unless = "#result == null || #result.isEmpty()")
    public List<Template> selectAllTemplateListByUserId(Long userId) {
        return templateMapper.selectAllTemplatesByUserId(userId);
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
    @Cacheable(value = "template-public", key = "#id", unless = "#result == null")
    public Template selectPublicTemplateById(Long id) {
        return templateMapper.selectPublicTemplateById(id);
    }

}
