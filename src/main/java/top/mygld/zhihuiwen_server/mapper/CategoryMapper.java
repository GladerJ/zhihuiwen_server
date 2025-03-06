package top.mygld.zhihuiwen_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.mygld.zhihuiwen_server.pojo.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {
    public List<Category> selectQuestionnaireCategoryByUsername(String username);
}




