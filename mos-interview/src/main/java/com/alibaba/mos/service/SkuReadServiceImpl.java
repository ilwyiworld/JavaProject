/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alibaba.mos.service;

import com.alibaba.mos.api.SkuReadService;
import com.alibaba.mos.data.SkuDO;
import com.alibaba.mos.util.ExcelUtil;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Service
public class SkuReadServiceImpl implements SkuReadService {

    /**
     * 这里假设 excel 数据量很大无法一次性加载到内存中
     * @param handler
     */
    @Override
    public void loadSkus(SkuHandler handler) {
        String fileName = this.getClass().getClassLoader().getResource("data/skus.xls").getPath();
        try {
            fileName = URLDecoder.decode(fileName,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ExcelUtil.readAllSheets(fileName, (result) -> {
            SkuDO skuDO = new SkuDO();
            ExcelUtil.excelToSkuDO(result, skuDO);
            if (skuDO.getId() != null) {
                handler.handleSku(skuDO);
            }
        });

    }
}