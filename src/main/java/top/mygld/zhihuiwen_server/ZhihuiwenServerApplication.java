package top.mygld.zhihuiwen_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"top.mygld.zhihuiwen_server", "com.anji.captcha"})
@SpringBootApplication
public class ZhihuiwenServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhihuiwenServerApplication.class, args);
    }

}
