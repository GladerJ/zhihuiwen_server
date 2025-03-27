package top.mygld.zhihuiwen_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String username;
    private String password;
    private String email;
    private String captcha;
    private String avatar;
    private Date createdAt;
    private Date updatedAt;
    private Long id;
}