package top.mygld.zhihuiwen_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireQuestion;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QuestionDTO implements Serializable {
    private String title;
    private String suggestion;
    private QuestionnaireQuestion question;
}
