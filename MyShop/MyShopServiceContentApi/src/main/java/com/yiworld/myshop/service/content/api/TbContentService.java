package com.yiworld.myshop.service.content.api;

import com.github.pagehelper.PageInfo;
import com.yiworld.myshop.commons.domain.TbContent;

public interface TbContentService {
    PageInfo<TbContent> page(int pageNum, int pageSize);
}