package com.yiworld.route.service.impl;

import com.yiworld.common.pojo.UserInfo;
import com.yiworld.route.service.UserInfoCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.yiworld.route.constant.Constant.ACCOUNT_PREFIX;
import static com.yiworld.route.constant.Constant.LOGIN_STATUS_PREFIX;

@Service
public class UserInfoCacheServiceImpl implements UserInfoCacheService {

    // 本地缓存，为了防止内存撑爆，后期可换为 LRU。
    private final static Map<Long, UserInfo> USER_INFO_MAP = new ConcurrentHashMap<>(64);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public UserInfo loadUserInfoByUserId(Long userId) {
        // 优先从本地缓存获取
        UserInfo userInfo = USER_INFO_MAP.get(userId);
        if (userInfo != null) {
            return userInfo;
        }
        //load redis
        String sendUserName = redisTemplate.opsForValue().get(ACCOUNT_PREFIX + userId);
        if (sendUserName != null) {
            userInfo = new UserInfo(userId, sendUserName);
            USER_INFO_MAP.put(userId, userInfo);
        }
        return userInfo;
    }

    @Override
    public boolean saveAndCheckUserLoginStatus(Long userId) throws Exception {
        Long add = redisTemplate.opsForSet().add(LOGIN_STATUS_PREFIX, userId.toString());
        if (add == 0) {
            // set 中已存在该 key
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void removeLoginStatus(Long userId) {
        redisTemplate.opsForSet().remove(LOGIN_STATUS_PREFIX, userId.toString());
    }

    @Override
    public Set<UserInfo> onlineUser() {
        Set<UserInfo> set = null;
        Set<String> members = redisTemplate.opsForSet().members(LOGIN_STATUS_PREFIX);
        for (String member : members) {
            if (set == null) {
                set = new HashSet<>(64);
            }
            UserInfo userInfo = loadUserInfoByUserId(Long.valueOf(member));
            set.add(userInfo);
        }
        return set;
    }
}
