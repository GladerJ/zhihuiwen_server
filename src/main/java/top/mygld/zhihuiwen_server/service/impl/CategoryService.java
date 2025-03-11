package top.mygld.zhihuiwen_server.service.impl;

import com.github.pagehelper.PageInfo;
import top.mygld.zhihuiwen_server.pojo.Category;

import java.util.List;

public interface CategoryService {
    public PageInfo<Category> selectQuestionnaireCategoryByUserId(Long userId,int pageNum, int pageSize);
    public void addCategory(Category category);
    public PageInfo<Category> selectQuestionnaireCategoryByUsername(String username,int pageNum, int pageSize);
    //判断该用户是否已创建相同命名分类
    public boolean checkQuestionnaireCategoryNameForCreate(Long userId,String name);
    //判断该用户修改的重命名是否存在
    public boolean checkQuestionnaireCategoryNameForUpdate(Long userId,String name,Long categoryId);


    public void deleteCategory(Category category);
    public void updateCategory(Category category);

    //定义模糊搜索
    public PageInfo<Category> selectQuestionnaireCategoryLike(Long userId,String content,int pageNum, int pageSize);

}
