package top.mygld.zhihuiwen_server.service.impl;

import top.mygld.zhihuiwen_server.common.Result;

public interface VerifyService {
    public Result<String> sendCode(String email, String captchaVerification);
    public Result<String> checkCode(String email,String code);
}
