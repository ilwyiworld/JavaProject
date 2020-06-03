package com.yiworld.client.vo.request;

import com.yiworld.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Function: Google Protocol 编解码发送
 */
@Data
public class GoogleProtocolVO extends BaseRequest {
    @NotNull(message = "requestId 不能为空")
    @ApiModelProperty(required = true, value = "requestId", example = "123")
    private Integer requestId ;

    @NotNull(message = "msg 不能为空")
    @ApiModelProperty(required = true, value = "msg", example = "hello")
    private String msg ;

    @Override
    public String toString() {
        return "GoogleProtocolVO{" +
                "requestId=" + requestId +
                ", msg='" + msg + '\'' +
                "} " + super.toString();
    }
}
