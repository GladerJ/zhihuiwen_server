package top.mygld.zhihuiwen_server.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import java.util.Date;
import java.util.List;

/**
* 问卷表
* @TableName questionnaire
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Questionnaire {

    /**
    * 问卷唯一标识
    */
    private Long id;
    /**
    * 分类ID
    */
    private Long categoryId;
    /**
    * 创建用户ID
    */
    private Long userId;
    /**
    * 问卷标题
    */
    private String title;
    /**
    * 问卷描述（限500字符）
    */
    private String description;
    /**
    * 问卷状态
    */
    private Object status;
    /**
    * 开始时间
    */
    private Date startTime;
    /**
    * 结束时间
    */
    private Date endTime;
    /**
    * 创建时间
    */
    private Date createdAt;
    /**
    * 更新时间
    */
    private Date updatedAt;
    private List<QuestionnaireQuestion> questions;

}
