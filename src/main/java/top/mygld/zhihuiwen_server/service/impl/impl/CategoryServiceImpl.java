package top.mygld.zhihuiwen_server.service.impl.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mygld.zhihuiwen_server.mapper.CategoryMapper;
import top.mygld.zhihuiwen_server.pojo.Category;
import top.mygld.zhihuiwen_server.service.impl.CategoryService;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public PageInfo<Category> selectQuestionnaireCategoryByUsername(String username, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Category> categories = categoryMapper.selectQuestionnaireCategoryByUsername(username);
        return new PageInfo<>(categories);
    }
}
