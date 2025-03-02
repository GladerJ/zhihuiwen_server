package top.mygld.zhihuiwen_server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.mygld.zhihuiwen_server.config.JWTConfig;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // 从请求头中获取 token，要求格式为 "Bearer token"
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // 调用 JWTConfig 中的方法解析 token 获取用户名
                String username = jwtConfig.getUsernameFromToken(token);
                // 如果解析成功则创建一个认证对象，权限列表为空
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                // 将认证信息放入 Spring Security 上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 记录异常日志，并抛出认证异常，交由 JwtAuthenticationEntryPoint 处理
                logger.error("JWT token 验证失败: " + e.getMessage());
                throw new BadCredentialsException("JWT token 验证失败: " + e.getMessage());
            }
        }
        // 没有 token 或 token 正常解析时，继续过滤链
        filterChain.doFilter(request, response);
    }
}
