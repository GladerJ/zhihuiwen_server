package top.mygld.zhihuiwen_server.pojo;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TotalReport implements Serializable {
    private Long id;
    private Long userId;
    private String content;
    private Date createdAt;
}
