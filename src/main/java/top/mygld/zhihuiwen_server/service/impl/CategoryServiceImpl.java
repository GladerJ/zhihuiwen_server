package top.mygld.zhihuiwen_server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mygld.zhihuiwen_server.mapper.CategoryMapper;
import top.mygld.zhihuiwen_server.mapper.QuestionnaireMapper;
import top.mygld.zhihuiwen_server.mapper.TemplateMapper;
import top.mygld.zhihuiwen_server.pojo.Category;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.pojo.User;
import top.mygld.zhihuiwen_server.service.CategoryService;
import top.mygld.zhihuiwen_server.service.TemplateService;
import top.mygld.zhihuiwen_server.service.UserService;
import top.mygld.zhihuiwen_server.service.QuestionnaireService;

import java.util.Date;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    UserService userService;

    @Autowired
    QuestionnaireService questionnaireService;

    @Autowired
    QuestionnaireMapper questionnaireMapper;

    @Autowired
    TemplateService templateService;

    @Autowired
    TemplateMapper templateMapper;

    @Override
    public List<Category> selectQuestionnaireCategoryByUserId(Long userId) {
        return categoryMapper.selectQuestionnaireCategoryByUserId(userId);
    }

    @Override
    public PageInfo<Category> selectQuestionnaireCategoryByUserId(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Category> categories = categoryMapper.selectQuestionnaireCategoryByUserId(userId);
        return new PageInfo<>(categories);
    }

    @Override
    public void addQuestionnaireCategory(Category category) {
        if (category.getCreatedAt() == null) category.setCreatedAt(new Date());
        if (category.getUpdatedAt() == null) category.setUpdatedAt(new Date());
        if (category.getCatalog() == null) category.setCatalog("questionnaire");
        categoryMapper.addCategory(category);
    }

    @Override
    public void addTemplateCategory(Category category) {
        if (category.getCreatedAt() == null) category.setCreatedAt(new Date());
        if (category.getUpdatedAt() == null) category.setUpdatedAt(new Date());
        if (category.getCatalog() == null) category.setCatalog("template");
        categoryMapper.addCategory(category);
    }

    @Override
    public PageInfo<Category> selectQuestionnaireCategoryByUsername(String username, int pageNum, int pageSize) {
        return selectQuestionnaireCategoryByUserId(userService.getUserIdByUsername(username), pageNum, pageSize);
    }

    @Override
    public boolean checkQuestionnaireCategoryNameForCreate(Long userId, String name) {
        if (categoryMapper.selectQuestionnaireCategoryByUserIdAndName(userId, name).size() > 0)
            return false;
        else
            return true;
    }

    @Override
    public boolean checkQuestionnaireCategoryNameForUpdate(Long userId, String name, Long categoryId) {
        List<Category> categories = categoryMapper.selectQuestionnaireCategoryByUserIdAndName(userId, name);
        if (categories.size() == 0) return true;
        if (categories.get(0).getId() == categoryId) return true;
        else return false;
    }

    @Override
    public void deleteCategory(Category category) {
        if (category.getCatalog() == "questionnaire") {
            questionnaireMapper.selectAllById(category.getUserId(), category.getId()).stream().forEach(
                    questionnaire -> {
                        questionnaireService.deleteQuestionnaire(questionnaire);
                    }
            );
        } else {
            templateMapper.selectAllById(category.getUserId(), category.getId()).stream().forEach(
                    template -> {
                        templateService.deleteTemplate(template);
                    }
            );
        }

        categoryMapper.deleteCategory(category);
    }

    @Override
    public void updateCategory(Category category) {
        if (category.getUpdatedAt() == null) category.setUpdatedAt(new Date());
        categoryMapper.updateCategory(category);
    }

    @Override
    public PageInfo<Category> selectQuestionnaireCategoryLike(Long userId, String content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Category> categories = categoryMapper.selectQuestionnaireCategoryLike(userId, content);
        return new PageInfo<>(categories);
    }

    @Override
    public List<Category> selectTemplateCategoryByUserId(Long userId) {
        return categoryMapper.selectTemplateCategoryByUserId(userId);
    }

    //Template部分
    @Override
    public PageInfo<Category> selectTemplateCategoryByUserId(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Category> categories = categoryMapper.selectTemplateCategoryByUserId(userId);
        return new PageInfo<>(categories);
    }


    @Override
    public PageInfo<Category> selectTemplateCategoryByUsername(String username, int pageNum, int pageSize) {
        return selectTemplateCategoryByUserId(userService.getUserIdByUsername(username), pageNum, pageSize);
    }

    @Override
    public boolean checkTemplateCategoryNameForCreate(Long userId, String name) {
        if (categoryMapper.selectTemplateCategoryByUserIdAndName(userId, name).size() > 0)
            return false;
        else
            return true;
    }

    @Override
    public boolean checkTemplateCategoryNameForUpdate(Long userId, String name, Long categoryId) {
        List<Category> categories = categoryMapper.selectTemplateCategoryByUserIdAndName(userId, name);
        if (categories.size() == 0) return true;
        if (categories.get(0).getId() == categoryId) return true;
        else return false;
    }

    @Override
    public PageInfo<Category> selectTemplateCategoryLike(Long userId, String content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Category> categories = categoryMapper.selectTemplateCategoryLike(userId, content);
        return new PageInfo<>(categories);
    }

}
