package com.yiworld.client.service.impl;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ClientInfo {

    private Info info = new Info() ;

    public Info get(){
        return info ;
    }

    public ClientInfo saveUserInfo(long userId,String userName){
        info.setUserId(userId);
        info.setUserName(userName);
        return this;
    }

    public ClientInfo saveServiceInfo(String serviceInfo){
        info.setServiceInfo(serviceInfo);
        return this;
    }

    public ClientInfo saveStartDate(){
        info.setStartDate(new Date());
        return this;
    }

    @NoArgsConstructor
    @Data
    public class Info{
        private String userName;
        private long userId ;
        private String serviceInfo ;
        private Date startDate ;
    }
}
