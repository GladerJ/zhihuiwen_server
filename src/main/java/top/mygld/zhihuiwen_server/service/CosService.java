package top.mygld.zhihuiwen_server.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CosService {
    // -1 表示上传文件大小无限制
    public static final long NO_LIMIT = -1;
    // 头像大小限制
    public static final long AVATAR_LIMIT = 1024 * 1024 * 2;
    public String uploadFile(MultipartFile file) throws IOException;
    public String uploadFileFromBase64(String base64Data, String fileName,long limit) throws IOException;
    public boolean deleteFile(String fileUrl);
}
