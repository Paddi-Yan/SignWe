package com.turing.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年10月30日 19:53:14
 */
@Getter
@Setter
public class SignVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String openid;

    private Integer chairId;

    private Double distance;


}
