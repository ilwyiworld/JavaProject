package com.yiworld.client.vo.request;

import com.yiworld.common.request.BaseRequest;
import lombok.Data;

@Data
public class LoginReqVO extends BaseRequest {
    private Long userId ;
    private String userName ;

    public LoginReqVO(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "LoginReqVO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                "} " + super.toString();
    }
}
