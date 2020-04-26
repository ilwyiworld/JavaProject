package com.yiworld.service;

import com.yiworld.common.ServerResponse;
import com.yiworld.pojo.Mail;

public interface TestService {

    ServerResponse testIdempotence();

    ServerResponse accessLimit();

    ServerResponse send(Mail mail);
}
