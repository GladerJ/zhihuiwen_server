package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {
    public int addCategory(Category category);
    public int deleteCategory(Category category);
    public int updateCategory(Category category);
    //问卷
    public List<Category> selectQuestionnaireCategoryByUserId(Long userId);
    public List<Category> selectQuestionnaireCategoryByUserIdAndName(Long userId,String name);
    public List<Category> selectQuestionnaireCategoryLike(Long userId,String content);
    //模板
    public List<Category> selectTemplateCategoryByUserId(Long userId);
    public List<Category> selectTemplateCategoryByUserIdAndName(Long userId, String name);
    public List<Category> selectTemplateCategoryLike(Long userId,String content);

}





