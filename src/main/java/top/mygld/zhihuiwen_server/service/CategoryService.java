package top.mygld.zhihuiwen_server.service;

import com.github.pagehelper.PageInfo;
import top.mygld.zhihuiwen_server.pojo.Category;

import java.util.List;

public interface CategoryService {
    public List<Category> selectQuestionnaireCategoryByUserId(Long userId);
    public PageInfo<Category> selectQuestionnaireCategoryByUserId(Long userId,int pageNum, int pageSize);
    public void addQuestionnaireCategory(Category category);
    public void addTemplateCategory(Category category);
    public PageInfo<Category> selectQuestionnaireCategoryByUsername(String username,int pageNum, int pageSize);
    //判断该用户是否已创建相同命名分类
    public boolean checkQuestionnaireCategoryNameForCreate(Long userId,String name);
    //判断该用户修改的重命名是否存在
    public boolean checkQuestionnaireCategoryNameForUpdate(Long userId,String name,Long categoryId);


    public void deleteCategory(Category category);
    public void updateCategory(Category category);

    //定义模糊搜索
    public PageInfo<Category> selectQuestionnaireCategoryLike(Long userId,String content,int pageNum, int pageSize);


    public List<Category> selectTemplateCategoryByUserId(Long userId);
    public PageInfo<Category> selectTemplateCategoryByUserId(Long userId,int pageNum, int pageSize);
    public PageInfo<Category> selectTemplateCategoryByUsername(String username,int pageNum, int pageSize);
    public boolean checkTemplateCategoryNameForCreate(Long userId,String name);
    public boolean checkTemplateCategoryNameForUpdate(Long userId,String name,Long categoryId);
    public PageInfo<Category> selectTemplateCategoryLike(Long userId,String content,int pageNum, int pageSize);
}
