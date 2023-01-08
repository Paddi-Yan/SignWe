package com.turing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaolyuh.annotation.CachePut;
import com.github.xiaolyuh.annotation.Cacheable;
import com.github.xiaolyuh.annotation.FirstCache;
import com.github.xiaolyuh.annotation.SecondaryCache;
import com.github.xiaolyuh.support.CacheMode;
import com.turing.common.RedisKey;
import com.turing.entity.Notice;
import com.turing.mapper.NoticeMapper;
import com.turing.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Cacheable(cacheNames = RedisKey.NOTICE_KEY, depict = "公告内容", cacheMode = CacheMode.ALL, key = "#id",
            firstCache = @FirstCache(expireTime = 7, timeUnit = TimeUnit.DAYS),
            secondaryCache = @SecondaryCache(expireTime = 7, timeUnit = TimeUnit.DAYS, forceRefresh = true))
    @Override
    public Notice getNotice(String id) {
        return noticeMapper.selectOne(null);
    }

    @CachePut(cacheNames = RedisKey.NOTICE_KEY, depict = "公告内容", cacheMode = CacheMode.ALL, key = "#id",
            firstCache = @FirstCache(initialCapacity = 1, maximumSize = 10, expireTime = 7, timeUnit = TimeUnit.DAYS),
            secondaryCache = @SecondaryCache(expireTime = 7, timeUnit = TimeUnit.DAYS, forceRefresh = true))
    @Override
    @Transactional
    public Notice updateNotice(Notice notice, String id) {
        noticeMapper.updateNotice(notice);
        Notice newNotice = noticeMapper.selectOne(null);
        return newNotice;
    }
}
