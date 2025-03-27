package top.mygld.zhihuiwen_server.service;

import com.github.pagehelper.PageInfo;
import top.mygld.zhihuiwen_server.pojo.Template;

public interface TemplateService {
    PageInfo<Template> selectAllById(Long userId, Long categoryId, int pageNum, int pageSize);
    int deleteTemplate(Template template);
    PageInfo<Template> selectTemplateLike(Long userId, Long categoryId, String content, int pageNum, int pageSize);
    int insertTemplate(Template template);
    int updateTemplate(Template template);
    Template selectTemplateByIdAndUserId(Long id, Long userId);
    Template selectTemplateById(Long id);
    boolean checkTemplateForUserId(Long userId, Long templateId);
    int addUsageCount(Long templateId);
    PageInfo<Template> selectAllPublicTemplates(int pageNum, int pageSize);
    PageInfo<Template> selectAllPublicTemplatesLike(String content, int pageNum, int pageSize);
    Template selectPublicTemplateById(Long id);
}
