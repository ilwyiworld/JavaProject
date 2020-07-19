package com.alibaba.mos.interview;

import com.alibaba.fastjson.JSON;
import com.alibaba.mos.api.ProviderConsumer;
import com.alibaba.mos.api.SkuReadService;
import com.alibaba.mos.data.ChannelInventoryDO;
import com.alibaba.mos.data.ItemDO;
import com.alibaba.mos.data.SkuDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
 */
@SpringBootTest
@Slf4j
class Interview2Tests {

    @Autowired
    SkuReadService skuReadService;

    @Autowired
    ProviderConsumer<List<ItemDO>> providerConsumer;

    /**
     * 试题1：
     * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
     * 在com.alibaba.mos.service.SkuReadServiceImpl中实现com.alibaba.mos.api.SkuReadService#loadSkus(com.alibaba.mos.api.SkuReadService.SkuHandler)
     * 从/resources/data/data.xls读取数据并逐条打印数据
     */
    @Test
    void readDataFromExcelWithHandlerTest() {
        AtomicInteger count = new AtomicInteger();
        skuReadService.loadSkus(skuDO -> {
            log.info("读取SKU信息={}", JSON.toJSONString(skuDO));
            count.incrementAndGet();
            return skuDO;
        });
        Assert.isTrue(count.get() == 10, "未能读取商品列表");
    }

    /**
     * 试题2：
     * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
     * 计算以下统计值:
     * 1、获取价格在最中间的任意一个skuId，假设所有sku的价格都是精确到1元且一定小于1万元
     * 2、每个渠道库存量为前五的skuId列表 例如( miao:[1,2,3,4,5],tmall:[3,4,5,6,7],intime:[7,8,4,3,1]
     * 3、所有sku的总价值
     */
    void statisticsDataTest() {
        AtomicInteger count = new AtomicInteger();
        List<SkuDO> skuDOList = new ArrayList<>();
        skuReadService.loadSkus(skuDO -> {
            count.incrementAndGet();
            skuDOList.add(skuDO);
            return skuDO;
        });
        Assert.isTrue(count.get() == 10, "未能读取商品列表");
        if (count.get() == 10) {
            // sku 总价
            BigDecimal skuCount = skuDOList.stream().map(SkuDO::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            // sku 均价
            BigDecimal average = skuCount.divide(BigDecimal.valueOf(skuDOList.size()), 2, BigDecimal.ROUND_HALF_UP);
            int minDifference = Math.abs(skuDOList.get(0).getPrice().intValue() - average.intValue());
            int minIndex = 0;
            // 通过与均值相减求得最中间的值
            for (int i = 1; i < skuDOList.size(); i++) {
                int temp = Math.abs(skuDOList.get(i).getPrice().intValue() - average.intValue());
                if (temp < minDifference) {
                    minIndex = i;
                    minDifference = temp;
                }
            }
            List<ChannelInventoryDO> cidList = new ArrayList<>();
            // 统计所有 Inventory
            for (int i = 0; i < skuDOList.size(); i++) {
                List<ChannelInventoryDO> list = skuDOList.get(i).getInventoryList();
                for (int ii = 0; ii < list.size(); ii++) {
                    cidList.add(list.get(ii));
                }
            }
            // 按照库存量降序
            cidList = cidList.stream().sorted(Comparator.comparing(ChannelInventoryDO::getInventory).reversed()).collect(Collectors.toList());
            // 按渠道分组
            Map<String, List<ChannelInventoryDO>> cidMap = cidList.stream().collect(Collectors.groupingBy(ChannelInventoryDO::getChannelCode));
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<ChannelInventoryDO>> entry : cidMap.entrySet()) {
                String channelCode = entry.getKey();
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(channelCode + ":[");
                List<ChannelInventoryDO> cList = entry.getValue();
                cList = cList.stream().limit(5).collect(Collectors.toList());
                StringBuilder sbn = new StringBuilder();
                for (int ii = 0; ii < cList.size(); ii++) {
                    if (sbn.length() > 0) {
                        sbn.append(",");
                    }
                    sbn.append(cList.get(ii).getSkuId());
                }
                sb.append(sbn.toString() + "]");
            }
            log.info("价格在最中间的任意一个 skuId = {}", skuDOList.get(minIndex).getId());
            log.info("每个渠道库存量为前五的 skuId列表 = {}", sb.toString());
            log.info("所有sku的总价值 = {}", skuCount);
        }
    }

    /**
     * 试题3:
     * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
     * 基于试题1, 在com.alibaba.mos.service.ItemAggregationProviderConsumer中实现一个生产者消费者, 将sku列表聚合为商品, 并通过回调函数返回,
     * 聚合规则为：
     * 对于sku type为原始商品(ORIGIN)的, 按货号(artNo)聚合成ITEM
     * 对于sku type为数字化商品(DIGITAL)的, 按spuId聚合成ITEM
     * 聚合结果需要包含: item的最大价格、最小价格、sku列表及总库存
     */
    @Test
    void aggregationSkusWithConsumerProviderTest() {
        AtomicInteger count = new AtomicInteger();
        providerConsumer.execute(list -> {
            list.forEach(item -> {
                log.info("聚合后ITEM信息={}", JSON.toJSONString(item));
                count.incrementAndGet();
            });
            return list;
        });
        Assert.isTrue(count.get() == 7, "未能聚合商品列表");
    }
}
