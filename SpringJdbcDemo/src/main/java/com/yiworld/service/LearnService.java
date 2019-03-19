package com.yiworld.service;

import com.yiworld.domain.LearnResouce;
import com.yiworld.tools.Page;

import java.util.Map;

public interface LearnService {
    int add(LearnResouce learnResouce);
    int update(LearnResouce learnResouce);
    int deleteByIds(String ids);
    LearnResouce queryLearnResouceById(Long learnResouce);
    Page queryLearnResouceList(Map<String, Object> params);
}
