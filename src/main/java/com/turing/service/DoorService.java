package com.turing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.Door;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface DoorService extends IService<Door> {

    Door getDoorStatus(String id);

    Door openDoor(Door door, String username);

    Door closeDoor(Door door, String username);
}
