package com.alibaba.mos.api;

import java.util.LinkedHashMap;

public interface ExcelReadHandler {
    // 读取一行的回调，每获取一条记录即写，而不缓存起来，减少内存的消耗
    void processOneRow(LinkedHashMap<String, String> row);
}
