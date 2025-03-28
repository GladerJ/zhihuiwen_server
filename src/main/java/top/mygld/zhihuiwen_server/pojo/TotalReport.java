package top.mygld.zhihuiwen_server.pojo;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TotalReport {
    private Long id;
    private Long userId;
    private String content;
    private Date createdAt;
}
