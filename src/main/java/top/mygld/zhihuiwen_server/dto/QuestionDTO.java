package top.mygld.zhihuiwen_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireQuestion;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QuestionDTO {
    private String title;
    private String suggestion;
    private QuestionnaireQuestion question;
}
