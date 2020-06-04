package com.yiworld.route;

import com.alibaba.fastjson.JSON;
import com.yiworld.route.api.vo.response.ServerResVO;
import com.yiworld.route.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = RouteApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class AccountServiceRedisImplTest {

    @Autowired
    private AccountService accountService ;

    @Test
    public void loadRouteRelated() throws Exception {
        for (int i = 0; i < 100; i++) {
            Map<Long, ServerResVO> longServerResVOMap = accountService.loadRouteRelated();
            log.info("longCIMServerResVOMap={},cun={}" , JSON.toJSONString(longServerResVOMap),i);
        }
        TimeUnit.SECONDS.sleep(10);
    }
}