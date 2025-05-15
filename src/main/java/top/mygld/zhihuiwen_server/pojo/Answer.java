package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Answer implements Serializable {
    private Long id;
    private Long responseId;
    private Long questionId;
    private String answerType;
    private Object answerContent;
    private Date createdAt;
}
