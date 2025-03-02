package top.mygld.zhihuiwen_server.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mygld.zhihuiwen_server.common.Result;

@RestController
@RequestMapping("/test")
public class TestController {
    @RequestMapping("/hello")
    public Result<String> hello() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return Result.success(username);
    }
}
