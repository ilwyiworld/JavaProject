package com.znv.hbase.util;

import com.google.protobuf.ByteString;
import com.znv.hbase.protobuf.generated.FilterProtos;
import com.znv.hbase.protobuf.generated.HBaseProtos;
import com.znv.hbase.protobuf.generated.HBaseProtos.NameBytesPair;
import com.znv.hbase.protobuf.generated.ScanProtos;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Consistency;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.io.TimeRange;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.util.ByteStringer;
import org.apache.hadoop.hbase.util.DynamicClassLoader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;

/**
 * Created by Administrator on 2016/10/18.
 */
public class ProtoUtil {
    /**
     * Convert a protocol buffer Scan to a client Scan
     *
     * @param proto the protocol buffer Scan to convert
     * @return the converted client Scan
     * @throws IOException
     */
    public static Scan toScan(final ScanProtos.Scan proto) throws IOException {
        byte[] startRow = HConstants.EMPTY_START_ROW;
        byte[] stopRow = HConstants.EMPTY_END_ROW;
        if (proto.hasStartRow()) {
            startRow = proto.getStartRow().toByteArray();
        }
        if (proto.hasStopRow()) {
            stopRow = proto.getStopRow().toByteArray();
        }
        Scan scan = new Scan(startRow, stopRow);
        if (proto.hasCacheBlocks()) {
            scan.setCacheBlocks(proto.getCacheBlocks());
        }
        if (proto.hasMaxVersions()) {
            scan.setMaxVersions(proto.getMaxVersions());
        }
        if (proto.hasStoreLimit()) {
            scan.setMaxResultsPerColumnFamily(proto.getStoreLimit());
        }
        if (proto.hasStoreOffset()) {
            scan.setRowOffsetPerColumnFamily(proto.getStoreOffset());
        }
        if (proto.hasLoadColumnFamiliesOnDemand()) {
            scan.setLoadColumnFamiliesOnDemand(proto.getLoadColumnFamiliesOnDemand());
        }
        if (proto.hasTimeRange()) {
            HBaseProtos.TimeRange timeRange = proto.getTimeRange();
            long minStamp = 0;
            long maxStamp = Long.MAX_VALUE;
            if (timeRange.hasFrom()) {
                minStamp = timeRange.getFrom();
            }
            if (timeRange.hasTo()) {
                maxStamp = timeRange.getTo();
            }
            scan.setTimeRange(minStamp, maxStamp);
        }
        if (proto.hasFilter()) {
            FilterProtos.Filter filter = proto.getFilter();
            scan.setFilter(ProtoUtil.toFilter(filter));
        }
        if (proto.hasBatchSize()) {
            scan.setBatch(proto.getBatchSize());
        }
        if (proto.hasMaxResultSize()) {
            scan.setMaxResultSize(proto.getMaxResultSize());
        }
        if (proto.hasSmall()) {
            scan.setSmall(proto.getSmall());
        }
        for (HBaseProtos.NameBytesPair attribute : proto.getAttributeList()) {
            scan.setAttribute(attribute.getName(), attribute.getValue().toByteArray());
        }
        if (proto.getColumnCount() > 0) {
            for (ScanProtos.Column column : proto.getColumnList()) {
                byte[] family = column.getFamily().toByteArray();
                if (column.getQualifierCount() > 0) {
                    for (ByteString qualifier : column.getQualifierList()) {
                        scan.addColumn(family, qualifier.toByteArray());
                    }
                } else {
                    scan.addFamily(family);
                }
            }
        }
        if (proto.hasReversed()) {
            scan.setReversed(proto.getReversed());
        }
        if (proto.hasConsistency()) {
            scan.setConsistency(toConsistency(proto.getConsistency()));
        }
        if (proto.hasCaching()) {
            scan.setCaching(proto.getCaching());
        }
        return scan;
    }

    /**
     * Convert a client Scan to a protocol buffer Scan
     *
     * @param scan the client Scan to convert
     * @return the converted protocol buffer Scan
     * @throws IOException
     */
    public static ScanProtos.Scan toScan(final Scan scan) throws IOException {
        ScanProtos.Scan.Builder scanBuilder = ScanProtos.Scan.newBuilder();
        scanBuilder.setCacheBlocks(scan.getCacheBlocks());
        if (scan.getBatch() > 0) {
            scanBuilder.setBatchSize(scan.getBatch());
        }
        if (scan.getMaxResultSize() > 0) {
            scanBuilder.setMaxResultSize(scan.getMaxResultSize());
        }
        if (scan.isSmall()) {
            scanBuilder.setSmall(scan.isSmall());
        }
        Boolean loadColumnFamiliesOnDemand = scan.getLoadColumnFamiliesOnDemandValue();
        if (loadColumnFamiliesOnDemand != null) {
            scanBuilder.setLoadColumnFamiliesOnDemand(loadColumnFamiliesOnDemand.booleanValue());
        }
        scanBuilder.setMaxVersions(scan.getMaxVersions());
        TimeRange timeRange = scan.getTimeRange();
        if (!timeRange.isAllTime()) {
            HBaseProtos.TimeRange.Builder timeRangeBuilder = HBaseProtos.TimeRange.newBuilder();
            timeRangeBuilder.setFrom(timeRange.getMin());
            timeRangeBuilder.setTo(timeRange.getMax());
            scanBuilder.setTimeRange(timeRangeBuilder.build());
        }
        Map<String, byte[]> attributes = scan.getAttributesMap();
        if (!attributes.isEmpty()) {
            NameBytesPair.Builder attributeBuilder = NameBytesPair.newBuilder();
            for (Map.Entry<String, byte[]> attribute : attributes.entrySet()) {
                attributeBuilder.setName(attribute.getKey());
                attributeBuilder.setValue(ByteStringer.wrap(attribute.getValue()));
                scanBuilder.addAttribute(attributeBuilder.build());
            }
        }
        byte[] startRow = scan.getStartRow();
        if (startRow != null && startRow.length > 0) {
            scanBuilder.setStartRow(ByteStringer.wrap(startRow));
        }
        byte[] stopRow = scan.getStopRow();
        if (stopRow != null && stopRow.length > 0) {
            scanBuilder.setStopRow(ByteStringer.wrap(stopRow));
        }
        if (scan.hasFilter()) {
            scanBuilder.setFilter(ProtoUtil.toFilter(scan.getFilter()));
        }
        if (scan.hasFamilies()) {
            ScanProtos.Column.Builder columnBuilder = ScanProtos.Column.newBuilder();
            for (Map.Entry<byte[], NavigableSet<byte[]>> family : scan.getFamilyMap().entrySet()) {
                columnBuilder.setFamily(ByteStringer.wrap(family.getKey()));
                NavigableSet<byte[]> qualifiers = family.getValue();
                columnBuilder.clearQualifier();
                if (qualifiers != null && qualifiers.size() > 0) {
                    for (byte[] qualifier : qualifiers) {
                        columnBuilder.addQualifier(ByteStringer.wrap(qualifier));
                    }
                }
                scanBuilder.addColumn(columnBuilder.build());
            }
        }
        if (scan.getMaxResultsPerColumnFamily() >= 0) {
            scanBuilder.setStoreLimit(scan.getMaxResultsPerColumnFamily());
        }
        if (scan.getRowOffsetPerColumnFamily() > 0) {
            scanBuilder.setStoreOffset(scan.getRowOffsetPerColumnFamily());
        }
        if (scan.isReversed()) {
            scanBuilder.setReversed(scan.isReversed());
        }
        if (scan.getConsistency() == Consistency.TIMELINE) {
            scanBuilder.setConsistency(toConsistency(scan.getConsistency()));
        }
        if (scan.getCaching() > 0) {
            scanBuilder.setCaching(scan.getCaching());
        }
        return scanBuilder.build();
    }

    /**
     * Primitive type to class mapping.
     */
    private static final Map<String, Class<?>> PRIMITIVES = new HashMap<String, Class<?>>();

    /**
     * Dynamic class loader to load filter/comparators
     */
    private static final ClassLoader CLASS_LOADER;

    static {
        ClassLoader parent = ProtobufUtil.class.getClassLoader();
        Configuration conf = HBaseConfiguration.create();
        CLASS_LOADER = new DynamicClassLoader(conf, parent);

        PRIMITIVES.put(Boolean.TYPE.getName(), Boolean.TYPE);
        PRIMITIVES.put(Byte.TYPE.getName(), Byte.TYPE);
        PRIMITIVES.put(Character.TYPE.getName(), Character.TYPE);
        PRIMITIVES.put(Short.TYPE.getName(), Short.TYPE);
        PRIMITIVES.put(Integer.TYPE.getName(), Integer.TYPE);
        PRIMITIVES.put(Long.TYPE.getName(), Long.TYPE);
        PRIMITIVES.put(Float.TYPE.getName(), Float.TYPE);
        PRIMITIVES.put(Double.TYPE.getName(), Double.TYPE);
        PRIMITIVES.put(Void.TYPE.getName(), Void.TYPE);
    }

    public static Consistency toConsistency(ScanProtos.Consistency consistency) {
        switch (consistency) {
            case STRONG:
                return Consistency.STRONG;
            case TIMELINE:
                return Consistency.TIMELINE;
            default:
                return Consistency.STRONG;
        }
    }

    public static ScanProtos.Consistency toConsistency(Consistency consistency) {
        switch (consistency) {
            case STRONG:
                return ScanProtos.Consistency.STRONG;
            case TIMELINE:
                return ScanProtos.Consistency.TIMELINE;
            default:
                return ScanProtos.Consistency.STRONG;
        }
    }

    @SuppressWarnings("unchecked")
    public static Filter toFilter(FilterProtos.Filter proto) throws IOException {
        String type = proto.getName();
        final byte[] value = proto.getSerializedFilter().toByteArray();
        String funcName = "parseFrom";
        try {
            Class<? extends Filter> c = (Class<? extends Filter>) Class.forName(type, true, CLASS_LOADER);
            Method parseFrom = c.getMethod(funcName, byte[].class);
            if (parseFrom == null) {
                throw new IOException("Unable to locate function: " + funcName + " in type: " + type);
            }
            return (Filter) parseFrom.invoke(c, value);
        } catch (Exception e) {
            // Either we couldn't instantiate the method object, or "parseFrom" failed.
            // In either case, let's not retry.
            throw new DoNotRetryIOException(e);
        }
    }

    public static FilterProtos.Filter toFilter(Filter filter) throws IOException {
        FilterProtos.Filter.Builder builder = FilterProtos.Filter.newBuilder();
        builder.setName(filter.getClass().getName());
        builder.setSerializedFilter(ByteStringer.wrap(filter.toByteArray()));
        return builder.build();
    }

}
