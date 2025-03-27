package top.mygld.zhihuiwen_server.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.mygld.zhihuiwen_server.service.CosService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

@Service
public class CosServiceImpl implements CosService {

    @Autowired
    private COSClient cosClient;

    @Value("${tencent.cos.bucketName}")
    private String bucketName;

    //直接上传文件
    public String uploadFile(MultipartFile file) throws IOException {
        String key = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File localFile = File.createTempFile("temp", null);
        file.transferTo(localFile);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        cosClient.putObject(putObjectRequest);
        localFile.delete();
        return cosClient.getObjectUrl(bucketName, key).toString();
    }

    /**
     * 上传 Base64 数据的图片，并返回外链
     * 使用公有读 Bucket，通过 COSClient.getObjectUrl 获取外链
     *
     * @param base64Data 包含前缀的数据，如 "data:image/png;base64,...."
     * @return 外链 URL 字符串
     * @throws IOException
     */
    public String uploadFileFromBase64(String base64Data, String fileName, long limit) throws IOException {
        String extension = "";
        if (base64Data.contains(",")) {
            String[] parts = base64Data.split(",");
            String mimePart = parts[0];
            base64Data = parts[1];
            if (mimePart.contains("image/")) {
                extension = mimePart.substring(mimePart.indexOf("image/") + 6, mimePart.indexOf(";"));
            }
        }
        if (!fileName.contains(".") && !extension.isEmpty()) {
            fileName = fileName + "." + extension;
        }
        byte[] fileBytes = Base64.getDecoder().decode(base64Data);
        if (limit != NO_LIMIT && fileBytes.length > limit) {
            return null;
        }
        MultipartFile multipartFile = new MockMultipartFile("file", fileName, "application/octet-stream", fileBytes);
        return uploadFile(multipartFile);
    }

    /**
     * 根据传入的外链 URL 删除对象存储中的图片
     *
     * @param fileUrl 外链 URL
     * @return 删除成功返回 true，否则返回 false
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // 解析 URL，获取对象 key
            URL url = new URL(fileUrl);
            String key = url.getPath();
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            // 删除对象
            cosClient.deleteObject(bucketName, key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
