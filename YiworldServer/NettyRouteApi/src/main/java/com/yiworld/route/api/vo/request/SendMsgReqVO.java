package com.yiworld.route.api.vo.request;

import com.yiworld.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SendMsgReqVO extends BaseRequest {

    @NotNull(message = "msg 不能为空")
    @ApiModelProperty(required = true, value = "msg", example = "hello")
    private String msg;

    @NotNull(message = "id 不能为空")
    @ApiModelProperty(required = true, value = "id", example = "11")
    private long id;
}
