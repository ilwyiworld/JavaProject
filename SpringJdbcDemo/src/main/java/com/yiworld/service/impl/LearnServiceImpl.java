package com.yiworld.service.impl;
import com.yiworld.dao.LearnDao;
import com.yiworld.domain.LearnResouce;
import com.yiworld.service.LearnService;
import com.yiworld.tools.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LearnServiceImpl implements LearnService {

    @Autowired
    LearnDao learnDao;

    /*//集成myBatis
    @Autowired
    LearnMapper learnDao;*/

    @Override
    public int add(LearnResouce learnResouce) {
        return this.learnDao.add(learnResouce);
    }

    @Override
    public int update(LearnResouce learnResouce) {
        return this.learnDao.update(learnResouce);
    }

    @Override
    public int deleteByIds(String ids) {
        return this.learnDao.deleteByIds(ids);
    }

    @Override
    public LearnResouce queryLearnResouceById(Long id) {
        return this.learnDao.queryLearnResouceById(id);
    }

    @Override
    public Page queryLearnResouceList(Map<String,Object> params) {
        //mybatis 分页插件 不需要使用 page 工具类了
        //PageHelper.startPage(Integer.parseInt(params.get("page").toString()), Integer.parseInt(params.get("rows").toString()));
        return this.learnDao.queryLearnResouceList(params);
    }
}
