package com.turing.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

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
@TableName("sys_ranking")
@ApiModel(value = "Ranking对象", description = "")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ranking implements Serializable {

    private static final long serialVersionUID = 103006776489737493L;
    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private Integer finalChair;

    @ApiModelProperty("打卡次数")
    @TableField(exist = false)
    private Integer count;

    @ApiModelProperty("总时长")
    private Integer totalTime;

    @ApiModelProperty("学习状态")
    @TableField(exist = false)
    private Boolean status;

    @ApiModelProperty("用户编号")
    @TableId
    private String id;

    @ApiModelProperty("用户openid")
    private String openid;

}
