package top.mygld.zhihuiwen_server.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Data
public class JWTConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expirationTime}")
    private long expirationTime;

    /**
     * 根据用户名生成 JWT 令牌
     * @param username 用户名
     * @return JWT 令牌字符串
     */
    public String generateToken(String username) {
        System.out.println(secretKey);
        return Jwts.builder()
                .setSubject(username)                      // 设置主题（可以是用户ID或用户名）
                .setIssuedAt(new Date())                     // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, secretKey) // 使用 HS256 算法进行签名
                .compact();                                  // 生成令牌
    }

    /**
     * 根据令牌获取用户名
     * @param token JWT 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
