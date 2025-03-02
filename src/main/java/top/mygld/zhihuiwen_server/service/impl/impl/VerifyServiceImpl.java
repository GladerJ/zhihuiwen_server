package top.mygld.zhihuiwen_server.service.impl.impl;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.service.impl.EmailSenderService;
import top.mygld.zhihuiwen_server.service.impl.RedisService;
import top.mygld.zhihuiwen_server.service.impl.VerifyService;
import top.mygld.zhihuiwen_server.utils.ValidCheckUtil;

@Service
public class VerifyServiceImpl implements VerifyService {
    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private RedisService redisService;
    public Result<String> sendCode(String email, String captchaVerification) {
        if(!ValidCheckUtil.isValidEmail(email))
            return Result.error("邮箱不合法");
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);
        if (response.isSuccess()){
            emailSenderService.sendVerificationCode(email);
            return Result.success("发送成功");
        }else {
            //验证码校验失败，返回信息告诉前端
            //repCode  0000  无异常，代表成功
            //repCode  9999  服务器内部异常
            //repCode  0011  参数不能为空
            //repCode  6110  验证码已失效，请重新获取
            //repCode  6111  验证失败
            //repCode  6112  获取验证码失败,请联系管理员
            String repCode = response.getRepCode();
            if(repCode.equals("9999"))
                return Result.error("服务器内部异常");
            if(repCode.equals("6110"))
                return Result.error("验证码已失效，请重新获取");
            if(repCode.equals("6111"))
                return Result.error("验证失败");
            return Result.error("获取验证码失败,请联系管理员");
        }
    }


    //检验验证码是否正确
    public Result<String> checkCode(String email,String code) {
        if (code == null){
            return Result.error("验证码不能为空");
        }
        if(redisService.getValue(email) == null){
            return Result.error("验证码输入错误或不存在");
        }
        redisService.deleteValue(email);
        return  Result.success("验证成功");
    }
}
