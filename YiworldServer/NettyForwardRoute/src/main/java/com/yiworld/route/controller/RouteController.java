package com.yiworld.route.controller;

import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.pojo.UserInfo;
import com.yiworld.common.pojo.RouteInfo;
import com.yiworld.common.response.BaseResponse;
import com.yiworld.common.response.NULLBody;
import com.yiworld.common.algorithm.RouteHandle;
import com.yiworld.common.util.RouteInfoParseUtil;
import com.yiworld.common.util.StringUtil;
import com.yiworld.route.api.RouteApi;
import com.yiworld.route.api.vo.request.ChatReqVO;
import com.yiworld.route.api.vo.request.LoginReqVO;
import com.yiworld.route.api.vo.request.P2PReqVO;
import com.yiworld.route.api.vo.request.RegisterInfoReqVO;
import com.yiworld.route.api.vo.response.ServerResVO;
import com.yiworld.route.api.vo.response.RegisterInfoResVO;
import com.yiworld.route.cache.ServerCache;
import com.yiworld.route.service.AccountService;
import com.yiworld.route.service.CommonBizService;
import com.yiworld.route.service.UserInfoCacheService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/")
@Slf4j
public class RouteController implements RouteApi {
    @Autowired
    private ServerCache serverCache;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserInfoCacheService userInfoCacheService;

    @Autowired
    private CommonBizService commonBizService;

    @Autowired
    private RouteHandle routeHandle;

    @ApiOperation("群聊 API")
    @RequestMapping(value = "groupRoute", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<NULLBody> groupRoute(@RequestBody ChatReqVO groupReqVO) throws Exception {
        BaseResponse<NULLBody> res = new BaseResponse();
        log.info("msg=[{}]", groupReqVO.toString());

        // 获取所有的推送列表
        Map<Long, ServerResVO> serverResVOMap = accountService.loadRouteRelated();
        for (Map.Entry<Long, ServerResVO> serverResVOEntry : serverResVOMap.entrySet()) {
            Long userId = serverResVOEntry.getKey();
            ServerResVO serverResVO = serverResVOEntry.getValue();
            if (userId.equals(groupReqVO.getUserId())) {
                // 过滤掉自己
                UserInfo userInfo = userInfoCacheService.loadUserInfoByUserId(groupReqVO.getUserId());
                log.warn("过滤掉了发送者 userId={}", userInfo.toString());
                continue;
            }
            // 推送消息
            ChatReqVO chatVO = new ChatReqVO(userId, groupReqVO.getMsg());
            accountService.pushMsg(serverResVO, groupReqVO.getUserId(), chatVO);
        }
        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }

    /**
     * 私聊路由
     *
     * @param p2pRequest
     * @return
     */
    @ApiOperation("私聊 API")
    @RequestMapping(value = "p2pRoute", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<NULLBody> p2pRoute(@RequestBody P2PReqVO p2pRequest) throws Exception {
        BaseResponse<NULLBody> res = new BaseResponse();
        try {
            // 获取接收消息用户的路由信息
            ServerResVO serverResVO = accountService.loadRouteRelatedByUserId(p2pRequest.getReceiveUserId());
            // p2pRequest.getReceiveUserId()==>消息接收者的 userID
            ChatReqVO chatVO = new ChatReqVO(p2pRequest.getReceiveUserId(), p2pRequest.getMsg());
            accountService.pushMsg(serverResVO, p2pRequest.getUserId(), chatVO);
            res.setCode(StatusEnum.SUCCESS.getCode());
            res.setMessage(StatusEnum.SUCCESS.getMessage());
        } catch (YiworldException e) {
            res.setCode(e.getErrorCode());
            res.setMessage(e.getErrorMessage());
        }
        return res;
    }


    @ApiOperation("客户端下线")
    @RequestMapping(value = "offLine", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<NULLBody> offLine(@RequestBody ChatReqVO groupReqVO) throws Exception {
        BaseResponse<NULLBody> res = new BaseResponse();
        UserInfo userInfo = userInfoCacheService.loadUserInfoByUserId(groupReqVO.getUserId());
        log.info("user [{}] offline!", userInfo.toString());
        accountService.offLine(groupReqVO.getUserId());
        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }

    /**
     * 获取一台  server
     *
     * @return
     */
    @ApiOperation("登录并获取服务器")
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<ServerResVO> login(@RequestBody LoginReqVO loginReqVO) throws Exception {
        BaseResponse<ServerResVO> res = new BaseResponse();

        // check server available
        String server = routeHandle.routeServer(serverCache.getServerList(), String.valueOf(loginReqVO.getUserId()));
        log.info("userName=[{}] route server info=[{}]", loginReqVO.getUserName(), server);

        RouteInfo routeInfo = RouteInfoParseUtil.parse(server);
        commonBizService.checkServerAvailable(routeInfo);

        // 登录校验
        StatusEnum status = accountService.login(loginReqVO);
        if (status == StatusEnum.SUCCESS) {
            // 保存路由信息
            accountService.saveRouteInfo(loginReqVO, server);
            ServerResVO vo = new ServerResVO(routeInfo);
            res.setDataBody(vo);
        }
        res.setCode(status.getCode());
        res.setMessage(status.getMessage());
        return res;
    }

    /**
     * 注册账号
     *
     * @return
     */
    @ApiOperation("注册账号")
    @RequestMapping(value = "registerAccount", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<RegisterInfoResVO> registerAccount(@RequestBody RegisterInfoReqVO registerInfoReqVO) throws Exception {
        BaseResponse<RegisterInfoResVO> res = new BaseResponse();
        long userId = System.currentTimeMillis();
        RegisterInfoResVO info = new RegisterInfoResVO(userId, registerInfoReqVO.getUserName());
        info = accountService.register(info);
        res.setDataBody(info);
        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }

    /**
     * 获取所有在线用户
     *
     * @return
     */
    @ApiOperation("获取所有在线用户")
    @RequestMapping(value = "onlineUser", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<Set<UserInfo>> onlineUser() throws Exception {
        BaseResponse<Set<UserInfo>> res = new BaseResponse();
        Set<UserInfo> userInfos = userInfoCacheService.onlineUser();
        res.setDataBody(userInfos);
        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }
}
