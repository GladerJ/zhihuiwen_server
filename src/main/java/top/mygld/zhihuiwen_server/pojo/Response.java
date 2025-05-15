package top.mygld.zhihuiwen_server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Response implements Serializable {
    private Long id;
    private Long questionnaireId;
    private Long userId;
    private Long duration;
    private String ipAddress;
    private String userAgent;
    private Date submittedAt;
    private List<Answer> answers;
    private Boolean isValid;
}
