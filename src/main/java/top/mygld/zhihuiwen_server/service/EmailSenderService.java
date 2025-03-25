package top.mygld.zhihuiwen_server.service.impl;

public interface EmailSenderService {
    public String generateVerificationCode(String recipientEmail);
    public void sendEmail(String recipientEmail, String subject, String content);
    public void sendVerificationCode(String recipientEmail);
}
