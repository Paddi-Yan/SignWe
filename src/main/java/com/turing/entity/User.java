package com.turing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
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
@TableName("sys_user")
@ApiModel(value = "User对象", description = "")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String openId;

    private Integer totalTime;

    private String classname;

    private Integer todayTime;

    private Integer errSignOut;

    private Integer finalChair;

    private Boolean finalCheck;

    private Integer finalDistance;

    private LocalDateTime finalStartTime;


}
