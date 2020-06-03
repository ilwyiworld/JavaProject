package com.yiworld.common.algorithm.consistenthash;

import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.exception.YiworldException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Function:TreeMap 实现
 */
public class TreeMapConsistentHash extends AbstractConsistentHash {
    private TreeMap<Long,String> treeMap = new TreeMap<Long, String>() ;

    /**
     * 虚拟节点数量
     */
    private static final int VIRTUAL_NODE_SIZE = 2 ;

    @Override
    public void add(long key, String value) {

        // fix https://github.com/crossoverJie/cim/issues/79
        treeMap.clear();
        for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
            Long hash = super.hash("vir" + key + i);
            treeMap.put(hash,value);
        }
        treeMap.put(key, value);
    }

    @Override
    public String getFirstNodeValue(String value) {
        long hash = super.hash(value);
        System.out.println("value=" + value + " hash = " + hash);
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if (!last.isEmpty()) {
            return last.get(last.firstKey());
        }
        if (treeMap.size() == 0){
            throw new YiworldException(StatusEnum.SERVER_NOT_AVAILABLE) ;
        }
        return treeMap.firstEntry().getValue();
    }
}