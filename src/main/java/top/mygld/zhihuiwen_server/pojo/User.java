package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    private Long id;
    private String avatar;
    private String email;
    private String username;
    private String password;
    private Date createdAt;
    private Date updatedAt;
}