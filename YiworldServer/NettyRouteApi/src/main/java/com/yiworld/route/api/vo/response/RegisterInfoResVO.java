package com.yiworld.route.api.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterInfoResVO {
    private Long userId ;
    private String userName ;

    @Override
    public String toString() {
        return "RegisterInfo{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                '}';
    }
}
