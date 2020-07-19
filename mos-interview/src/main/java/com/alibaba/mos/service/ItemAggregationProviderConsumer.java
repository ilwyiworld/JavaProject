/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alibaba.mos.service;

import com.alibaba.mos.api.ProviderConsumer;
import com.alibaba.mos.api.SkuReadService;
import com.alibaba.mos.data.ChannelInventoryDO;
import com.alibaba.mos.data.ItemDO;
import com.alibaba.mos.data.SkuDO;
import com.alibaba.mos.util.CollectorsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemAggregationProviderConsumer implements ProviderConsumer<List<ItemDO>> {
    @Autowired
    SkuReadService skuReadService;

    @Override
    public void execute(ResultHandler<List<ItemDO>> handler) {
        List<ItemDO> result = new ArrayList<>();
        List<SkuDO> skuDOList = new ArrayList<>();
        skuReadService.loadSkus(skuDO -> {
            skuDOList.add(skuDO);
            return skuDO;
        });
        if (skuDOList.size() > 0) {
            Map<String, List<SkuDO>> skuMap = skuDOList.stream().collect(Collectors.groupingBy(SkuDO::getSkuType));
            for (Map.Entry<String, List<SkuDO>> entry : skuMap.entrySet()) {
                String skuType = entry.getKey();
                List<SkuDO> sList = entry.getValue();
                if (skuType.equals("ORIGIN")) {
                    Map<String, List<SkuDO>> artNoMap = sList.stream().collect(Collectors.groupingBy(SkuDO::getArtNo));
                    for (List<SkuDO> vList : artNoMap.values()) {
                        result.add(getItemDO(vList));
                    }
                } else if (skuType.equals("DIGITAL")) {
                    Map<String, List<SkuDO>> spuIdMap = sList.stream().collect(Collectors.groupingBy(SkuDO::getSpuId));
                    for (List<SkuDO> vList : spuIdMap.values()) {
                        result.add(getItemDO(vList));
                    }
                }
            }
        }
        handler.handleResult(result);
    }

    private ItemDO getItemDO(List<SkuDO> vList) {
        ItemDO itemDO = new ItemDO();
        Optional<SkuDO> min = vList.stream().min(Comparator.comparing(SkuDO::getPrice));
        Optional<SkuDO> max = vList.stream().max(Comparator.comparing(SkuDO::getPrice));
        itemDO.setMinPrice(min.get().getPrice());
        itemDO.setMaxPrice(max.get().getPrice());
        itemDO.setName(min.get().getName());
        itemDO.setArtNo(min.get().getArtNo());
        itemDO.setSpuId(min.get().getSpuId());
        List<String> skuIds = new ArrayList<>();
        BigDecimal inventory = new BigDecimal(0.0);
        for (int i = 0; i < vList.size(); i++) {
            SkuDO skuDO = vList.get(i);
            skuIds.add(skuDO.getId());
            List<ChannelInventoryDO> iList = skuDO.getInventoryList();
            BigDecimal big = iList.stream().collect(CollectorsUtil.summingBigDecimal(ChannelInventoryDO::getInventory));
            inventory = inventory.add(big);
        }
        itemDO.setInventory(inventory);
        itemDO.setSkuIds(skuIds);
        return itemDO;
    }
}