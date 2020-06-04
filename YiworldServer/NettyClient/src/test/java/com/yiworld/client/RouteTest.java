package com.yiworld.client;

import com.yiworld.client.service.RouteRequest;
import com.yiworld.client.vo.request.LoginReqVO;
import com.yiworld.client.vo.response.ServerResVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest(classes = ClientApplication.class)
@RunWith(SpringRunner.class)
public class RouteTest {

    @Value("${yiworld.user.id}")
    private long userId;

    @Value("${yiworld.user.userName}")
    private String userName;

    @Autowired
    private RouteRequest routeRequest ;

    @Test
    public void test() throws Exception {
        LoginReqVO vo = new LoginReqVO(userId,userName) ;
        ServerResVO.ServerInfo server = routeRequest.getServer(vo);
        log.info("Server=[{}]",server.toString());
    }
}
