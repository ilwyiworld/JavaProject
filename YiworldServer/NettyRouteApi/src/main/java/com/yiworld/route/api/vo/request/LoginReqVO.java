package com.yiworld.route.api.vo.request;

import com.yiworld.common.request.BaseRequest;
import lombok.Data;

@Data
public class LoginReqVO extends BaseRequest {
    private Long userId ;
    private String userName ;

    @Override
    public String toString() {
        return "LoginReqVO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                "} " + super.toString();
    }
}