package top.mygld.zhihuiwen_server.controller;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.pojo.Category;
import top.mygld.zhihuiwen_server.pojo.User;
import top.mygld.zhihuiwen_server.service.impl.CategoryService;
import top.mygld.zhihuiwen_server.service.impl.UserService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    UserService userService;
    @RequestMapping("/selectQuestionnaireCategory")
    public Result<PageInfo<Category>> selectQuestionnaireCategoryByUserId(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize) {
        Long userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(categoryService.selectQuestionnaireCategoryByUserId(userId,pageNum,pageSize));
    }

    @RequestMapping("/create/checkQuestionnaireCategoryName")
    public Result<String> checkQuestionnaireCategoryNameForCreate(@RequestBody Category category) {
        Long userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = categoryService.checkQuestionnaireCategoryNameForCreate(userId, category.getName());
        if(!flag) return Result.error("分类名已存在");
        return Result.success("分类名可用");
    }

    @RequestMapping("/addQuestionnaireCategory")
    public Result<String> addQuestionnaireCategory(@RequestBody Category category) {
        Long userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = categoryService.checkQuestionnaireCategoryNameForCreate(userId, category.getName());
        if(!flag) return Result.error("分类名已存在");
        category.setUserId(userId);
        categoryService.addCategory(category);
        return Result.success("添加成功");
    }

    @RequestMapping("/deleteCategory")
    public Result<String> deleteCategory(@RequestBody Category category) {
        Long userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        category.setUserId(userId);
        categoryService.deleteCategory(category);
        return Result.success("删除成功");
    }

    @RequestMapping("/updateCategory")
    public Result<String> updateCategory(@RequestBody Category category) {
        Long userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        category.setUserId(userId);
        categoryService.updateCategory(category);
        return Result.success("修改成功");
    }

    @RequestMapping("/update/checkQuestionnaireCategoryName")
    public Result<String> checkQuestionnaireCategoryNameForUpdate(@RequestBody Category category) {
        Long userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean flag = categoryService.checkQuestionnaireCategoryNameForUpdate(userId, category.getName(),category.getId());
        if(!flag) return Result.error("分类名已存在");
        return Result.success("分类名可用");
    }

    //模糊搜索接口
    @RequestMapping("/selectQuestionnaireCategoryLike")
    public Result<PageInfo<Category>> selectQuestionnaireCategoryLike(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam String content) {
        Long userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(categoryService.selectQuestionnaireCategoryLike(userId,content,pageNum,pageSize));
    }

//    @GetMapping("/getCategoryById")
//    public Result<Category> getCategoryById(Category category) {
//        Long userId = (Long)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        category.setUserId(userId);
//        return Result.success(categoryService.selectQuestionnaireCategoryLike(userId,content,pageNum,pageSize));
//    }
}
