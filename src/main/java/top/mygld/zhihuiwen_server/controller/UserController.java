package top.mygld.zhihuiwen_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.dto.RegisterDTO;
import top.mygld.zhihuiwen_server.pojo.User;
import top.mygld.zhihuiwen_server.service.impl.RedisService;
import top.mygld.zhihuiwen_server.service.impl.UserService;
import top.mygld.zhihuiwen_server.service.impl.VerifyService;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    VerifyService verifyService;

    @RequestMapping("/register")
    public Result<String> registerUser(@RequestBody RegisterDTO registerDTO) {
        if(registerDTO.getUsername().length() > 16)
            return Result.error("用户名不可超过16");
        if(registerDTO.getUsername().length() < 5)
            return Result.error("用户名长度不可低于5");
        String usernamePattern = "^[A-Za-z0-9]+$";
        if (!Pattern.matches(usernamePattern, registerDTO.getUsername())) {
            return Result.error("用户名只能包含字母和数字");
        }
        if(userService.selectUserByUsername(registerDTO.getUsername()).size() > 0){
            return Result.error("用户名已存在");
        }
        String code = registerDTO.getCaptcha();
        String email = registerDTO.getEmail();
        if (code == null){
            return Result.error("验证码不能为空");
        }
        Result<String> result = verifyService.checkCode(email, code);
        if (!result.getCode().equals(200)){
           return result;
        }
        userService.insertUser(new User(null, registerDTO.getAvatar(), registerDTO.getEmail(), registerDTO.getUsername(), registerDTO.getPassword()));
        return Result.success("注册成功");
    }

    @RequestMapping("/verify/send")
    public Result<String> sendVerifyCode(String email, String captchaVerification) {
        if(userService.selectUserByEmail(email).size() > 0){
            return Result.error("邮箱已被注册");
        }
        return verifyService.sendCode(email, captchaVerification);
    }
}
