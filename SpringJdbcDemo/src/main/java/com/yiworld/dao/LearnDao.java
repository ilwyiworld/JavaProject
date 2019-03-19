package com.yiworld.dao;

import com.yiworld.domain.LearnResouce;
import com.yiworld.tools.Page;

import java.util.Map;

public interface LearnDao {
    int add(LearnResouce learnResouce);
    int update(LearnResouce learnResouce);
    int deleteByIds(String ids);
    LearnResouce queryLearnResouceById(Long id);
    Page queryLearnResouceList(Map<String, Object> params);
}
