package com.turing.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: SignWe
 * @CreatedTime: 2023年01月08日 23:55:45
 */
@Data
@ToString
public class ScrollResult<T> implements Serializable {
    private static final long serialVersionUID = 8439680298189923829L;
    private List<T> data;
    private Long minTime;
    private Long offset;
    private Boolean hasData;
}
