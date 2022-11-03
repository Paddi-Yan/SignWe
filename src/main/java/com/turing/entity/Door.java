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
@TableName("sys_door")
@ApiModel(value = "Door对象", description = "")
public class Door implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isOpen;

    private String closeInfo;

    private String openInfo;

    private String id;


}
