package com.yiworld.route;

import com.alibaba.fastjson.JSON;
import com.yiworld.common.pojo.UserInfo;
import com.yiworld.route.service.UserInfoCacheService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@SpringBootTest(classes = RouteApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class UserInfoCacheServiceImplTest {

    @Autowired
    private UserInfoCacheService userInfoCacheService;

    @Test
    public void checkUserLoginStatus() throws Exception {
        boolean status = userInfoCacheService.saveAndCheckUserLoginStatus(2000L);
        log.info("status={}", status);
    }

    @Test
    public void removeLoginStatus() throws Exception {
        userInfoCacheService.removeLoginStatus(2000L);
    }

    @Test
    public void onlineUser(){
        Set<UserInfo> userInfos = userInfoCacheService.onlineUser();
        log.info("UserInfos={}", JSON.toJSONString(userInfos));
    }

}