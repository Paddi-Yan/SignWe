package com.turing.signwe;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.turing.common.RedisKey;
import com.turing.entity.Chairs;
import com.turing.entity.Record;
import com.turing.entity.User;
import com.turing.mapper.ChairsMapper;
import com.turing.mapper.RecordMapper;
import com.turing.mapper.UserMapper;
import com.turing.service.ChairsService;
import com.turing.service.RecordService;
import com.turing.service.UserService;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class SignWeApplicationTests {


    @Resource
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", "o8Mur5ZcwFb5R2GBesmKqeQXJD1w"));
        System.out.println(user);
        System.out.println(user.getName());
    }

    @Resource
    private ChairsService chairsService;

    @Test
    void getChairsListTest() {
        System.out.println(chairsService.getById(2));
    }

    @Resource
    private RecordService recordService;

    @Test
    void getRecordListTest() {
        User user = new User();
        user.setClassname("电子1201");
        user.setName("颜昭琰");
        recordService.getRecordByUser(user).forEach(System.out :: println);
    }

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void yesterdayRecordTest() {
        Record record = (Record) redisTemplate.opsForList()
                                              .leftPop("user_study_record_电子1201颜昭琰");
        System.out.println(record);
    }

    @Test
    void yesterdayRankingTest() {
        redisTemplate.delete(RedisKey.YESTERDAY_RANKING_ZSET_KEY);
    }

    public static void main(String[] args) {
        String s = "key_123";
        s = s.replaceAll("key_", "");
        System.out.println(s.toString());
    }

    @Resource
    private ChairsMapper chairsMapper;

    @Test
    void chairsHash1Test() {
//        List<Chairs> chairsList = chairsMapper.selectList(null);
//        HashMap hashMap = new HashMap<>();
//        for(Chairs chairs : chairsList) {
//            hashMap.put(""+chairs.getId(), chairs);
//        }
//        redisTemplate.opsForHash().putAll("chairs_hash",hashMap);

        long start = System.currentTimeMillis();
        List<Chairs> chairs = redisTemplate.opsForHash().values("chairs_hash");

        chairs.sort(Comparator.comparingInt(Chairs :: getId));
        //chairs.forEach(System.out :: println);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    void deleteRecordTest() {
        recordService.deleteLogical();
    }

    @Resource
    private UserService userService;

    @Resource
    private RecordMapper recordMapper;
    @Test
    void selectRecordTest() {
        for(Record record : recordService.getRecordByUser(userService.getByOpenId("o8Mur5ZcwFb5R2GBesmKqeQXJD1w"))) {
            System.out.println(record);
        }
    }

}
