package top.mygld.zhihuiwen_server.pojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 统一分类表（问卷/模板）
 * @TableName category
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    /**
     * 分类唯一标识
     */
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类目录类型
     */
    private Object catalog;

    /**
     * 分类描述（限500字符）
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 问卷数量
     */
    private Long questionnaireCount;

}