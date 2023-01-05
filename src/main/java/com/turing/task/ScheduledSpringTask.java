package com.turing.task;

import com.turing.entity.Chairs;
import com.turing.entity.Door;
import com.turing.entity.vo.SignOutVo;
import com.turing.mapper.DoorMapper;
import com.turing.service.ChairsService;
import com.turing.service.DoorService;
import com.turing.service.YesterdayRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年10月30日 11:52:18
 */
@Component
@EnableScheduling
@Slf4j
public class ScheduledSpringTask {

    @Resource
    private DoorMapper doorMapper;

    @Resource
    private DoorService doorService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ChairsService chairsService;

    @Resource
    private YesterdayRankingService yesterdayRecordService;

    private static final String ID = "TuringTeam";

    /**
     * 定时调度任务列表
     * 1.自动关门
     * 2.所有位置置为空闲,未签退的结算签到时长
     * 3.隔天将所有人的当天学习时间改为0(已并入2.任务)
     * 4.每天更新昨天的学习排行榜
     * 5.每天删除Redis中的昨日学习记录(已并入4.任务)
     * 补充:清除Redis中的昨日学习记录,但是会持久化在MySQL,提高Redis的缓存命中率,因为这部分缓存更新完昨日排行榜
     * 之后就不会再用到
     */

    @Scheduled(cron = "30 30 23 * * ? ")
    public void closeDoorTask() throws InterruptedException {
        Door door = doorService.getDoorStatus(ID);
        if(door.getOpen()) {
            doorService.closeDoor(door, "11:30 自动关门");
            log.info("实验室自动关门成功!");
        }
    }


    @Scheduled(cron = "00 30 23 * * ? ")
    public void autoSignOut() throws InterruptedException {
        ScheduledSpringTask.log.info("下班时间到,青蒜签到时间中....");
        List<Chairs> chairsList = chairsService.getChairsList();
        for(Chairs chair : chairsList) {
            //遍历座位,如果有未签退的自动签退并计算学习时长
            if(!chair.getIsEmpty()) {
                SignOutVo signOutVo = new SignOutVo(chair.getOpenId(), chair.getId());
                ScheduledSpringTask.log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                ScheduledSpringTask.log.info("自动签退信息:{}", signOutVo);
                ScheduledSpringTask.log.info("座位信息:{}", chair);
                ScheduledSpringTask.log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                try {
                    chairsService.signOut(signOutVo);
                } catch(Exception e) {
                    ScheduledSpringTask.log.error("签退失败,原因:" + e.getMessage());
                }
            }
        }
        ScheduledSpringTask.log.info("青蒜签到时间成功!");
    }

    @Scheduled(cron = "0 0 0 * * ? ")
    public void updateYesterdayRanking() throws InterruptedException {
        ScheduledSpringTask.log.info("计算昨日学习排行榜定时任务开启....");
        yesterdayRecordService.generateYesterdayRanking();
    }


}
