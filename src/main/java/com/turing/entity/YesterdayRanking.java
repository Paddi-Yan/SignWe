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
@TableName("sys_yesterday_record")
@ApiModel(value = "YesterdayRecord对象", description = "")
public class YesterdayRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer studyTime;

    private String username;


}
