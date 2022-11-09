package com.turing.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年10月29日 20:03:53
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String openid;

    private String classname;

    private String name;
}
