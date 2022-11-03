package com.turing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 学习记录历史
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Getter
@Setter
@TableName("sys_record")
@ApiModel(value = "Record对象", description = "学习记录历史")
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer chair;

    @ApiModelProperty("打卡距离")
    private Integer distance;

    private String name;

    private String classname;

    private LocalDateTime finalStartTime;

    private LocalDateTime finalStopTime;

    private Integer studyTime;


}
