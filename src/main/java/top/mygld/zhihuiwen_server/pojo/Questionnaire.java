package top.mygld.zhihuiwen_server.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import java.util.Date;

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
    private Integer id;
    /**
    * 来源模板ID
    */
    private Integer templateId;
    /**
    * 分类ID
    */
    private Integer categoryId;
    /**
    * 创建用户ID
    */
    private Integer userId;
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

}
