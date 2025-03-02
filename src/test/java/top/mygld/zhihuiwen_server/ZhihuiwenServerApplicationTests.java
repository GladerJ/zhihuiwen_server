package top.mygld.zhihuiwen_server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mygld.zhihuiwen_server.service.impl.CosService;
import top.mygld.zhihuiwen_server.service.impl.EmailSenderService;
import top.mygld.zhihuiwen_server.service.impl.RedisService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ZhihuiwenServerApplicationTests {

    @Autowired
    private EmailSenderService emailSenderService;
    @Test
    void contextLoads() {
        emailSenderService.sendVerificationCode("1417344239@qq.com");
        System.out.println("发送成功");
    }

    @Autowired
    private RedisService redisService;

    @Autowired
    private CosService cosService;
    @Test
    void test1(){
        //redisService.setValueWithExpiry("test", "test", 60, TimeUnit.SECONDS);
        System.out.println(redisService.getValue("test"));
    }


}
