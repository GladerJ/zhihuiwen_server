package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireQuestion {
    private Long id;
    private Long questionnaireId;
    private String questionText;
    private Object questionType;
    private short sortOrder;
    private List<QuestionnaireOption> options;
}
