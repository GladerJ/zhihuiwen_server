package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QuestionnaireOption {
    private Long id;
    private Long questionId;
    private String optionText;
    private short sortOrder;
    private Long count;
}
