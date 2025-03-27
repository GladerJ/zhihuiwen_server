package top.mygld.zhihuiwen_server.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import top.mygld.zhihuiwen_server.service.EmailSenderService;

import java.util.Random;

import org.springframework.mail.javamail.JavaMailSender;
import top.mygld.zhihuiwen_server.service.RedisService;

@Service
public class EmailSenderServiceImpl implements EmailSenderService {


    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RedisService redisService;

    // 生成6位随机验证码
    public String generateVerificationCode(String email) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        redisService.setValueWithExpiry(email, code.toString(), 5, java.util.concurrent.TimeUnit.MINUTES);
        return code.toString();
    }

    // 发送邮件
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
        } catch (MailException e) {
            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }

    // 发送验证码邮件
    public void sendVerificationCode(String recipientEmail) {
        String code = generateVerificationCode(recipientEmail);
        String subject = "智慧问注册验证";
        String content = "您的验证码是：" + code + "\n请在5分钟内完成验证。";
        sendEmail(recipientEmail, subject, content);
    }
}
