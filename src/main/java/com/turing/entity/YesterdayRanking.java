package com.turing.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import lombok.*;

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
@TableName("sys_yesterday_ranking")
@ApiModel(value = "YesterdayRanking对象", description = "")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class YesterdayRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer studyTime;

    private String username;


}
