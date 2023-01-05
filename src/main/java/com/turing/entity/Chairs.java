package com.turing.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Getter
@Setter
@TableName("sys_chairs")
@ApiModel(value = "Chairs对象", description = "")
@ToString
public class Chairs implements Serializable {

    private static final long serialVersionUID = 1L;

    private String lastUsedName;

    @TableField("is_empty")
    private Boolean isEmpty;

    private Integer id;

    @TableField("openid")
    private String openId;

    @Version
    //@TableField(fill = FieldFill.INSERT)
    private Integer version;

}
