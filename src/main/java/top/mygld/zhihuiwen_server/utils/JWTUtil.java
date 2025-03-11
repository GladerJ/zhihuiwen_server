package top.mygld.zhihuiwen_server.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {

    // 定义静态变量
    private static String secretKey;
    private static long expirationTime;

    // 使用 setter 方法注入配置值到静态变量中
    @Value("${jwt.secret}")
    public void setSecretKey(String secretKey) {
        JWTUtil.secretKey = secretKey;
    }

    @Value("${jwt.expirationTime}")
    public void setExpirationTime(long expirationTime) {
        JWTUtil.expirationTime = expirationTime;
    }

    /**
     * 根据用户名生成 JWT 令牌
     * @param userId 用户Id
     * @return JWT 令牌字符串
     */
    public static String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())  // 设置主题（可以是用户ID或用户名）
                .setIssuedAt(new Date())  // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, secretKey)  // 使用 HS256 算法进行签名
                .compact();
    }

    /**
     * 根据令牌获取用户名
     * @param token JWT 令牌
     * @return 用户id
     */
    public static Long getUserIdFromToken(String token) {
        return Long.valueOf(Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }
}
