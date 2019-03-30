package com.yiworld.DubboSpringBootDemoConsumer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yiworld.common.People;
import com.yiworld.common.PeopleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PeopleController {

    @Reference
    private PeopleService peopleService;
    @GetMapping("/people/{name}")
    public People getPeople(@PathVariable("name") String name) {
        People people = new People();
        people.setName(name);
        return peopleService.getPeople(people);
    }
}