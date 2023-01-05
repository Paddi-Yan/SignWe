package com.turing.common;

/**
 * @Author: 又蠢又笨的懒羊羊程序猿
 * @CreateTime: 2022年01月20日 01:55:30
 */
public class RedisKey {
    public static final String KEY_PREFIX = "signWe:";
    public static final String DOOR_KEY = KEY_PREFIX + "door:status";
    public static final String NOTICE_KEY = KEY_PREFIX + "notice";

    public static final String RANKING_HASH_KEY = KEY_PREFIX + "ranking_hash";
    public static final String RANKING_FIELD_KEY = KEY_PREFIX + "ranking_user_id_";

    public static final String RANKING_ZSET_KEY = KEY_PREFIX + "ranking_zset";

    public static final String YESTERDAY_RANKING_KEY = KEY_PREFIX + "yesterday_ranking";

    public static final String RECORD_KEY = KEY_PREFIX + "study_record:";

    public static final String USER_HASH_KEY = KEY_PREFIX + "user:";

    public static final String USER_FILED_KEY = "user_openid_";

    public static final String CHAIRS_HASH_KEY = KEY_PREFIX + "chairs:";
    public static final String CHAIRS_FIELD_KEY = "id:";

    public static final String REDISSON_LOCK_PREFIX = KEY_PREFIX + "lock:";

    public static final String USER_KEY = KEY_PREFIX + "user";

    public static final String TURING_TEAM = "TuringTeam";

}
