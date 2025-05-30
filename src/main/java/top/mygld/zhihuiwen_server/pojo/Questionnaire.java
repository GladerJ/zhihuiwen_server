package top.mygld.zhihuiwen_server.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* 问卷表
* @TableName questionnaire
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Questionnaire implements Serializable{

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
    private String status;
    /**
    * 开始时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    /**
    * 结束时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
