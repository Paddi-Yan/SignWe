package com.turing.service;

import com.turing.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface NoticeService extends IService<Notice> {

    Notice getNotice();

    Notice updateNotice(Notice notice);
}
