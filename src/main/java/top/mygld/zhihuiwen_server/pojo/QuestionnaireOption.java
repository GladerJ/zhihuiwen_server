package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QuestionnaireOption implements Serializable{
    private Long id;
    private Long questionId;
    private String optionText;
    private short sortOrder;
    private Long count;
}
