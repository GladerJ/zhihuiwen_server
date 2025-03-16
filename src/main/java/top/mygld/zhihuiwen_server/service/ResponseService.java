package top.mygld.zhihuiwen_server.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import top.mygld.zhihuiwen_server.pojo.Response;

public interface ResponseService {
    @Transactional
    Response saveResponse(Response response, HttpServletRequest request);
}
