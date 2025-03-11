package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {
    public List<Category> selectQuestionnaireCategoryByUserId(Long userId);
    public int addCategory(Category category);
    public List<Category> selectQuestionnaireCategoryByUserIdAndName(Long userId,String name);
    public int deleteCategory(Category category);
    public int updateCategory(Category category);
    //模糊搜索
    public List<Category> selectQuestionnaireCategoryLike(Long userId,String content);
}




