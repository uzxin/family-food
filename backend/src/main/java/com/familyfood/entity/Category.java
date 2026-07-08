package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜品分类表
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 家庭ID */
    private Long familyId;

    /** 分类编码 */
    private String code;

    /** 分类名称 */
    private String name;

    /** 分类图标emoji */
    private String icon;

    /** 排序序号 */
    private Integer sortOrder;

    /** 是否内置分类：0=自定义, 1=内置 */
    private Integer isBuiltin;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
