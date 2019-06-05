package com.yiworld.myshop.service.content.api;

import com.github.pagehelper.PageInfo;
import com.yiworld.myshop.commons.domain.TbContent;

import java.util.List;

public interface TbContentService {
    PageInfo<TbContent> page(int pageNum, int pageSize);
}