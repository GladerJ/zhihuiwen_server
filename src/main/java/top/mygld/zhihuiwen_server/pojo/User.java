package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String avatar;
    private String email;
    private String username;
    private String password;
}
