package com.turing.service.impl;

import com.turing.entity.Notice;
import com.turing.mapper.NoticeMapper;
import com.turing.service.NoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

}
