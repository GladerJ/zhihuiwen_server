package top.mygld.zhihuiwen_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.dto.UserDTO;
import top.mygld.zhihuiwen_server.pojo.User;
import top.mygld.zhihuiwen_server.service.CosService;
import top.mygld.zhihuiwen_server.service.UserService;
import top.mygld.zhihuiwen_server.service.VerifyService;
import top.mygld.zhihuiwen_server.utils.JWTUtil;
import top.mygld.zhihuiwen_server.utils.ValidCheckUtil;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
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

    @RequestMapping("/update")
    public Result<String> updateUser(@RequestBody UserDTO userDTO) {
        // 获取当前登录用户ID
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 如果上传了新头像
        String url = null;
        if (userDTO.getAvatar() != null) {
            try {
                url = cosService.uploadFileFromBase64(userDTO.getAvatar(), userId + "_avatar", cosService.AVATAR_LIMIT);
                if (url == null) {
                    return Result.error("上传失败,文件大小不能超过2M！");
                }
            } catch (IOException e) {
                return Result.error("上传头像失败");
            }
        }

        //删除原来的头像在对象存储中
        User original = userService.getUserById(userId);
        cosService.deleteFile(original.getAvatar());

        User user = new User();
        user.setId(userId);
        user.setAvatar(url);  // 可能为空，表示不更新头像
        user.setUpdatedAt(new Date());

        userService.updateUserProfile(user);
        return Result.success("更新成功");
    }

    @RequestMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody UserDTO userDTO) {
        // 获取当前登录用户ID
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User existingUser = userService.getUserById(userId);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
    // 验证验证码
        String email = userDTO.getEmail();
        String code = userDTO.getCaptcha();
        Result<String> result = verifyService.checkCode(email, code);
        if (!result.getCode().equals(200)) {
            return result;
        }
        // 更新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(userDTO.getPassword());
        updateUser.setUpdatedAt(new Date());

        userService.updateUserPassword(updateUser);
        return Result.success("密码修改成功");
    }

    @RequestMapping("/updateEmail")
    public Result<String> updateEmail(@RequestBody UserDTO userDTO) {
        // 获取当前登录用户ID
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 验证邮箱验证码
        String email = userDTO.getEmail();
        String code = userDTO.getCaptcha();

        // 检查邮箱是否已被注册
        if (userService.selectUserByEmail(email) != null) {
            return Result.error("该邮箱已被注册");
        }

        // 验证验证码
        Result<String> result = verifyService.checkCode(email, code);
        if (!result.getCode().equals(200)) {
            return result;
        }

        // 更新邮箱
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setEmail(email);
        updateUser.setUpdatedAt(new Date());

        userService.updateUserEmail(updateUser);
        return Result.success("邮箱更新成功");
    }
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
        if(userService.selectUserByUsername(userDTO.getUsername()) != null){
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
            userService.insertUser(new User(null, "https://b0.bdstatic.com/0df6c8c7f109aa7b67e7cb15e6f8d025.jpg@h_1280", userDTO.getEmail(), userDTO.getUsername(), userDTO.getPassword(), new Date(), new Date()));
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
        userService.insertUser(new User(null, url, userDTO.getEmail(), userDTO.getUsername(), userDTO.getPassword(),new Date(),new Date()));
        return Result.success("注册成功");
    }

    @RequestMapping("/verify/sendForRegister")
    public Result<String> sendVerifyCode(String email, String captchaVerification) {
        if(userService.selectUserByEmail(email) != null){
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
        if(userService.selectUserByUsernameAndPassword(userDTO.getUsername(), userDTO.getPassword()) == null){
            return Result.error("用户名或密码错误");
        }
        String token = JWTUtil.generateToken(userService.getUserIdByUsername(userDTO.getUsername()));
        return Result.success(token);
    }

    @RequestMapping("/profile")
    public Result<UserDTO> getProfile() {
        User user = userService.getUserById((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (user == null) return Result.error("用户不存在");
        return Result.success(new UserDTO(user.getUsername(), null, user.getEmail(), null, user.getAvatar(),null,null,null));
    }

    @RequestMapping("/loginByEmail")
    public Result<String> loginByEmail(@RequestBody UserDTO userDTO) {
        String code = userDTO.getCaptcha();
        String email = userDTO.getEmail();
        if (code == null){
            return Result.error("验证码不能为空");
        }
        if(!ValidCheckUtil.isValidEmail(email)){
            return Result.error("邮箱不合法");
        }
        Result<String> result = verifyService.checkCode(email, code);
        if (!result.getCode().equals(200)){
            return result;
        }
        User user = userService.selectUserByEmail(email);
        if (user == null) return Result.error("用户不存在");
        String token = JWTUtil.generateToken(user.getId());
        return Result.success(token);
    }

    @RequestMapping("/verify/sendForLogin")
    public Result<String> sendForLogin(String email, String captchaVerification) {
        if(userService.selectUserByEmail(email) == null){
            return Result.error("邮箱尚未被注册，请先注册");
        }
        return verifyService.sendCode(email, captchaVerification);
    }



}
