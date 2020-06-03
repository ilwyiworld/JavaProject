package com.yiworld.client.vo.request;

import com.yiworld.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Function: 群发请求
 */
@Data
public class GroupReqVO extends BaseRequest {

    @NotNull(message = "userId 不能为空")
    @ApiModelProperty(required = true, value = "消息发送者的 userId", example = "1545574049323")
    private Long userId ;


    @NotNull(message = "msg 不能为空")
    @ApiModelProperty(required = true, value = "msg", example = "hello")
    private String msg ;

    public GroupReqVO(Long userId, String msg) {
        this.userId = userId;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "GroupReqVO{" +
                "userId=" + userId +
                ", msg='" + msg + '\'' +
                "} " + super.toString();
    }
}
