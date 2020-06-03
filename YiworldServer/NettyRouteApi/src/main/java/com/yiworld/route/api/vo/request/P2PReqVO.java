package com.yiworld.route.api.vo.request;

import com.yiworld.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Function: 单聊请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2PReqVO extends BaseRequest {

    @NotNull(message = "userId 不能为空")
    @ApiModelProperty(required = true, value = "消息发送者的 userId", example = "1545574049323")
    private Long userId ;

    @NotNull(message = "userId 不能为空")
    @ApiModelProperty(required = true, value = "消息接收者的 userId", example = "1545574049323")
    private Long receiveUserId ;

    @NotNull(message = "msg 不能为空")
    @ApiModelProperty(required = true, value = "msg", example = "hello")
    private String msg ;

    @Override
    public String toString() {
        return "GroupReqVO{" +
                "userId=" + userId +
                ", msg='" + msg + '\'' +
                "} " + super.toString();
    }
}