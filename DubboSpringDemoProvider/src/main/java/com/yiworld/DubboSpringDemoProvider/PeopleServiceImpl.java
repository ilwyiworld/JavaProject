package com.yiworld.DubboSpringDemoProvider;

import com.alibaba.dubbo.config.annotation.Service;
import com.yiworld.common.People;
import com.yiworld.common.PeopleService;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PeopleServiceImpl implements PeopleService {
    private static final Logger logger = LoggerFactory.getLogger(PeopleServiceImpl.class);

    /**
     * 这个方法的作用是接收传过来的People实体，将其ID赋为随机数
     */
    @Override
    public People getPeople(People people) {
        people.setId(new Random().nextInt(10000));
        logger.debug("People:{},ID:{}", people.getName(), people.getId());
        return people;
    }
}