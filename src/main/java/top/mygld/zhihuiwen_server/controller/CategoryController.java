package top.mygld.zhihuiwen_server.controller;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.pojo.Category;
import top.mygld.zhihuiwen_server.service.impl.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @RequestMapping("/selectQuestionnaireCategory")
    public Result<PageInfo<Category>> selectQuestionnaireCategoryByUsername(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return Result.success(categoryService.selectQuestionnaireCategoryByUsername(username,pageNum,pageSize));
    }
}
