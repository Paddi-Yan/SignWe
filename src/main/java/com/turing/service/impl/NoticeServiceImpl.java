package com.turing.service.impl;

import com.turing.common.RedisKey;
import com.turing.entity.Notice;
import com.turing.mapper.NoticeMapper;
import com.turing.service.NoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Resource
    private NoticeMapper noticeMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Notice getNotice() {
        Notice notice = (Notice) redisTemplate.opsForValue().get(RedisKey.NOTICE_KEY);
        if(notice != null) {
            return notice;
        }
        notice = noticeMapper.selectOne(null);
        redisTemplate.opsForValue().set(RedisKey.NOTICE_KEY, notice);
        return notice;
    }

    @Override
    public Notice updateNotice(Notice notice) {
        noticeMapper.updateNotice(notice);
        Notice newNotice = noticeMapper.selectOne(null);
        redisTemplate.opsForValue().set(RedisKey.NOTICE_KEY, newNotice);
        return newNotice;
    }
}
