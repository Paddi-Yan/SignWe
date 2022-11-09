package com.turing.controller;

import com.google.common.collect.ImmutableMap;
import com.turing.entity.User;
import com.turing.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年11月09日 15:06:02
 */
@RestController
@RequestMapping("/api")
public class ExceptionController {

    @GetMapping("/notFound")
    public void throwException() {
        User user = new User();
        user.setId("123213");
        throw new ResourceNotFoundException(ImmutableMap.of("userId", user.getId()));
    }
}
