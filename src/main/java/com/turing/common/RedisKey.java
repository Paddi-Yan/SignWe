package com.turing.common;

/**
 * @Author: 又蠢又笨的懒羊羊程序猿
 * @CreateTime: 2022年01月20日 01:55:30
 */
public class RedisKey {
    public static final String DOOR_KEY = "door_status";
    public static final String NOTICE_KEY = "notice";

    public static final String RANKING_HASH_KEY = "ranking_hash";
    public static final String RANKING_FIELD_KEY = "ranking_user_id_";

    public static final String RANKING_ZSET_KEY = "ranking_zset";

    public static final String YESTERDAY_RANKING_ZSET_KEY = "yesterday_ranking_zset";

    public static final String RECORD_KEY = "user_study_record_";

    public static final String USER_HASH_KEY = "users";

    public static final String USER_FILED_KEY = "user_openid_";

    public static final String CHAIRS_HASH_KEY = "chairs";
    public static final String CHAIRS_FIELD_KEY = "chair_id_";

}
