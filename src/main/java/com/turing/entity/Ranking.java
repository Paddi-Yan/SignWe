package com.turing.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@TableName("sys_ranking")
@ApiModel(value = "Ranking对象", description = "")
@AllArgsConstructor
@NoArgsConstructor
public class Ranking implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Integer finalChair;

    @ApiModelProperty("打卡次数")
    private Integer count;

    @ApiModelProperty("总时长")
    private Integer totalTime;

    @ApiModelProperty("学习状态")
    private Boolean status;

    @ApiModelProperty("用户编号")
    private String id;


}
