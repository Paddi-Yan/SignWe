package com.turing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

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
public class Chairs implements Serializable {

    private static final long serialVersionUID = 1L;

    private String lastUsedName;

    private Boolean isEmpty;

    private Integer id;

    private String openId;


}
