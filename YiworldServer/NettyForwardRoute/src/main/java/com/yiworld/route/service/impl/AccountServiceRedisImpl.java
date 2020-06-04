package com.yiworld.route.service.impl;

import com.yiworld.common.proxy.ProxyManager;
import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.pojo.UserInfo;
import com.yiworld.common.util.RouteInfoParseUtil;
import com.yiworld.common.util.StringUtil;
import com.yiworld.route.api.vo.request.ChatReqVO;
import com.yiworld.route.api.vo.request.LoginReqVO;
import com.yiworld.route.api.vo.response.ServerResVO;
import com.yiworld.route.api.vo.response.RegisterInfoResVO;
import com.yiworld.route.service.AccountService;
import com.yiworld.route.service.UserInfoCacheService;
import com.yiworld.server.api.ServerApi;
import com.yiworld.server.api.vo.request.SendMsgReqVO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.yiworld.common.enums.StatusEnum.OFF_LINE;
import static com.yiworld.route.constant.Constant.ACCOUNT_PREFIX;
import static com.yiworld.route.constant.Constant.ROUTE_PREFIX;

@Service
@Slf4j
public class AccountServiceRedisImpl implements AccountService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserInfoCacheService userInfoCacheService;

    @Autowired
    private OkHttpClient okHttpClient;

    @Override
    public RegisterInfoResVO register(RegisterInfoResVO info) {
        String key = ACCOUNT_PREFIX + info.getUserId();
        String name = redisTemplate.opsForValue().get(info.getUserName());
        if (null == name) {
            // 为了方便查询，冗余一份
            redisTemplate.opsForValue().set(key, info.getUserName());
            redisTemplate.opsForValue().set(info.getUserName(), key);
        } else {
            long userId = Long.parseLong(name.split(":")[1]);
            info.setUserId(userId);
            info.setUserName(info.getUserName());
        }
        return info;
    }

    @Override
    public StatusEnum login(LoginReqVO loginReqVO) throws Exception {
        // 去 Redis 里查询
        String key = ACCOUNT_PREFIX + loginReqVO.getUserId();
        String userName = redisTemplate.opsForValue().get(key);
        if (null == userName) {
            return StatusEnum.ACCOUNT_NOT_MATCH;
        }
        if (!userName.equals(loginReqVO.getUserName())) {
            return StatusEnum.ACCOUNT_NOT_MATCH;
        }

        // 登录成功，保存登录状态
        boolean status = userInfoCacheService.saveAndCheckUserLoginStatus(loginReqVO.getUserId());
        if (status == false) {
            // 重复登录
            return StatusEnum.REPEAT_LOGIN;
        }
        return StatusEnum.SUCCESS;
    }

    @Override
    public void saveRouteInfo(LoginReqVO loginReqVO, String msg) throws Exception {
        String key = ROUTE_PREFIX + loginReqVO.getUserId();
        redisTemplate.opsForValue().set(key, msg);
    }

    @Override
    public Map<Long, ServerResVO> loadRouteRelated() {
        Map<Long, ServerResVO> routes = new HashMap<>(64);
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        ScanOptions options = ScanOptions.scanOptions()
                .match(ROUTE_PREFIX + "*")
                .build();
        Cursor<byte[]> scan = connection.scan(options);

        while (scan.hasNext()) {
            byte[] next = scan.next();
            String key = new String(next, StandardCharsets.UTF_8);
            log.info("key={}", key);
            parseServerInfo(routes, key);
        }
        try {
            scan.close();
        } catch (IOException e) {
            log.error("IOException", e);
        }
        return routes;
    }

    @Override
    public ServerResVO loadRouteRelatedByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(ROUTE_PREFIX + userId);
        if (value == null) {
            throw new YiworldException(OFF_LINE);
        }
        ServerResVO cimServerResVO = new ServerResVO(RouteInfoParseUtil.parse(value));
        return cimServerResVO;
    }

    private void parseServerInfo(Map<Long, ServerResVO> routes, String key) {
        long userId = Long.valueOf(key.split(":")[1]);
        String value = redisTemplate.opsForValue().get(key);
        ServerResVO cimServerResVO = new ServerResVO(RouteInfoParseUtil.parse(value));
        routes.put(userId, cimServerResVO);
    }

    @Override
    public void pushMsg(ServerResVO serverResVO, long sendUserId, ChatReqVO groupReqVO) {
        UserInfo userInfo = userInfoCacheService.loadUserInfoByUserId(sendUserId);
        String url = "http://" + serverResVO.getIp() + ":" + serverResVO.getHttpPort();
        ServerApi serverApi = new ProxyManager<>(ServerApi.class, url, okHttpClient).getInstance();
        SendMsgReqVO vo = new SendMsgReqVO(userInfo.getUserName() + ":" + groupReqVO.getMsg(), groupReqVO.getUserId());
        Response response = null;
        try {
            response = (Response) serverApi.sendMsg(vo);
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            response.body().close();
        }
    }

    @Override
    public void offLine(Long userId) throws Exception {
        // TODO: 改为一个原子命令，以防数据一致性
        // 删除路由
        redisTemplate.delete(ROUTE_PREFIX + userId);
        // 删除登录状态
        userInfoCacheService.removeLoginStatus(userId);
    }
}
