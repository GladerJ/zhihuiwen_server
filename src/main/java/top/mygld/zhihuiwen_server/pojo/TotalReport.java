package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@AllArgsConstructor
@NonNull
@ToString
public class TotalReport {
    private Long id;
    private Long userId;
    private String content;
}
