package top.mygld.zhihuiwen_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.dto.UserDTO;
import top.mygld.zhihuiwen_server.pojo.User;
import top.mygld.zhihuiwen_server.service.impl.CosService;
import top.mygld.zhihuiwen_server.service.impl.UserService;
import top.mygld.zhihuiwen_server.service.impl.VerifyService;
import top.mygld.zhihuiwen_server.utils.JWTUtil;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    VerifyService verifyService;

    @Autowired
    CosService cosService;

    //注册用户
    @RequestMapping("/register")
    public Result<String> registerUser(@RequestBody UserDTO userDTO) {
        if(userDTO.getUsername().length() > 16)
            return Result.error("用户名不可超过16");
        if(userDTO.getUsername().length() < 5)
            return Result.error("用户名长度不可低于5");
        String usernamePattern = "^[A-Za-z0-9]+$";
        if (!Pattern.matches(usernamePattern, userDTO.getUsername())) {
            return Result.error("用户名只能包含字母和数字");
        }
        if(userService.selectUserByUsername(userDTO.getUsername()).size() > 0){
            return Result.error("用户名已存在");
        }
        String code = userDTO.getCaptcha();
        String email = userDTO.getEmail();
        if (code == null){
            return Result.error("验证码不能为空");
        }
        Result<String> result = verifyService.checkCode(email, code);
        if (!result.getCode().equals(200)){
           return result;
        }
        if (userDTO.getAvatar() == null){
            userService.insertUser(new User(null, "https://b0.bdstatic.com/0df6c8c7f109aa7b67e7cb15e6f8d025.jpg@h_1280", userDTO.getEmail(), userDTO.getUsername(), userDTO.getPassword()));
            return Result.success("注册成功");
        }
        String url = null;
        try {
            url = cosService.uploadFileFromBase64(userDTO.getAvatar(), userDTO.getUsername() + "_avatar",cosService.AVATAR_LIMIT);
            if(url == null){
                return Result.error("上传失败,文件大小不能超过2M！");
            }
        } catch (IOException e) {
            return Result.error("上传失败");
        }
        userService.insertUser(new User(null, url, userDTO.getEmail(), userDTO.getUsername(), userDTO.getPassword()));
        return Result.success("注册成功");
    }

    @RequestMapping("/verify/sendForRegister")
    public Result<String> sendVerifyCode(String email, String captchaVerification) {
        if(userService.selectUserByEmail(email).size() > 0){
            return Result.error("邮箱已被注册");
        }
        return verifyService.sendCode(email, captchaVerification);
    }

    //通过密码登录账号
    @RequestMapping("/loginByPassword")
    public Result<String> loginByPassword(@RequestBody UserDTO userDTO) {
        String usernamePattern = "^[A-Za-z0-9]+$";
        if (!Pattern.matches(usernamePattern, userDTO.getUsername())) {
            return Result.error("用户名只能包含字母和数字");
        }
        if(userService.selectUserByUsernameAndPassword(userDTO.getUsername(), userDTO.getPassword()).size() == 0){
            return Result.error("用户名或密码错误");
        }
        String token = JWTUtil.generateToken(userDTO.getUsername());
        return Result.success(token);
    }

    @RequestMapping("/profile")
    public Result<UserDTO> getProfile() {
        List<User> users = userService.selectUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (users.size() == 0) return Result.error("用户不存在");
        return Result.success(new UserDTO(users.get(0).getUsername(), null, users.get(0).getEmail(), null, users.get(0).getAvatar()));
    }

}
