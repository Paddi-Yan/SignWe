package com.turing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.Notice;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface NoticeService extends IService<Notice> {

    Notice getNotice(String id);

    Notice updateNotice(Notice notice, String id);
}
