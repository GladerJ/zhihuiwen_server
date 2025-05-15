package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TemplateQuestion implements Serializable {
    private Long id;
    private Long templateId;
    private String questionText;
    private Object questionType;
    private short sortOrder;
    private List<TemplateOption> options;
}
