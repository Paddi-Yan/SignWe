package com.turing.entity;

import lombok.*;

import java.io.Serializable;

/**
 * @Author: Paddi-Yan
 * @Project: SignWe
 * @CreatedTime: 2023年01月13日 23:06:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class StatisticsInfo implements Serializable {
    private static final long serialVersionUID = -8523274004046713157L;

    private String userId;

    private Integer keepSignInDays;
}
