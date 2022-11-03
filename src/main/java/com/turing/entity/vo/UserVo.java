package com.turing.entity.vo;

import com.turing.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年10月29日 19:22:14
 */
@Getter
@Setter
public class UserVo extends User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Boolean isNewGuys;

    public void transform(User user) {
        if(user == null) {
            isNewGuys = true;
        } else {
            isNewGuys = false;
            BeanUtils.copyProperties(user, this);
        }
    }
}
