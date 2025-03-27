package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TemplateQuestion {
    private Long id;
    private Long templateId;
    private String questionText;
    private Object questionType;
    private short sortOrder;
    private List<TemplateOption> options;
}
