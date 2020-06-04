package com.yiworld.server.controller;

import com.yiworld.common.constant.Constants;
import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.response.BaseResponse;
import com.yiworld.server.api.ServerApi;
import com.yiworld.server.api.vo.request.SendMsgReqVO;
import com.yiworld.server.api.vo.response.SendMsgResVO;
import com.yiworld.server.server.Server;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class IndexController implements ServerApi {

    @Autowired
    private Server server;

    /**
     * 统计 service
     */
    @Autowired
    private CounterService counterService;

    /**
     * @param sendMsgReqVO
     * @return
     */
    @Override
    @ApiOperation("Push msg to client")
    @RequestMapping(value = "sendMsg",method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse<SendMsgResVO> sendMsg(@RequestBody SendMsgReqVO sendMsgReqVO){
        BaseResponse<SendMsgResVO> res = new BaseResponse();
        server.sendMsg(sendMsgReqVO) ;
        counterService.increment(Constants.COUNTER_SERVER_PUSH_COUNT);
        SendMsgResVO sendMsgResVO = new SendMsgResVO() ;
        sendMsgResVO.setMsg("OK") ;
        res.setCode(StatusEnum.SUCCESS.getCode()) ;
        res.setMessage(StatusEnum.SUCCESS.getMessage()) ;
        res.setDataBody(sendMsgResVO) ;
        return res ;
    }

}
