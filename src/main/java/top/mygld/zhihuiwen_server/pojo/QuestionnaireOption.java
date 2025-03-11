package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireOption {
    private Long id;
    private Long questionId;
    private String optionText;
    private short sortOrder;
}
