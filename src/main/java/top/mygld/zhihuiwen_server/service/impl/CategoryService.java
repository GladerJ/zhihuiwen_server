package top.mygld.zhihuiwen_server.service.impl;

import com.github.pagehelper.PageInfo;
import top.mygld.zhihuiwen_server.pojo.Category;

import java.util.List;

public interface CategoryService {
    public PageInfo<Category> selectQuestionnaireCategoryByUsername(String username,int pageNum, int pageSize);
}
