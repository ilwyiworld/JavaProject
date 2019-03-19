package com.yiworld.hbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IOUtils;

public class HbaseOperation {
	
	public static HTable getHTableByTableName(String tableName) throws IOException{
		//Get instance of Default Configuration
		Configuration conf= HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "106.14.193.13");
		conf.set("hbase.zookeeper.property.clientPort","2181");
		
		//Get table instance
		@SuppressWarnings("deprecation")
		HTable table=new HTable(conf, tableName);
		return table;
	}
	
	public static void getData() throws IOException{
		String tableName="user";
		HTable table=getHTableByTableName(tableName);
		
		//Create Get by rowkey
		Get get=new Get(Bytes.toBytes("0624"));
		
		//add column
		get.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"));
		get.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"));
		
		//Get Date
		Result result=table.get(get);
		
		//key: rowkey + cf + c + version
		//value
		Cell[] cells=result.rawCells();
		for (Cell cell : cells) {
			System.out.println(		//
				Bytes.toString(CellUtil.cloneFamily(cell))+":"+	//
				Bytes.toString(CellUtil.cloneQualifier(cell))+":"+ //
				Bytes.toString(CellUtil.cloneValue(cell))+":"
			);
		}
		
		//Table Close
		table.close();
	}
	
	@SuppressWarnings("deprecation")
	public static void putData() throws IOException{
		String tableName="user";
		HTable table=getHTableByTableName(tableName);
		
		Put put=new Put(Bytes.toBytes("10101"));
		
		//Add a column with value
		put.add(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes("yiliang"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("age"), Bytes.toBytes(12));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("address"), Bytes.toBytes("shanghai"));
		table.put(put);
		IOUtils.closeStream(table);
	}
	
	public static void scan() throws IOException{
		String tableName="user";
		HTable table=getHTableByTableName(tableName);
		
		Scan scan=new Scan();
		
		//Range
		scan.setStartRow(Bytes.toBytes("10001"));
		scan.setStartRow(Bytes.toBytes("10006"));
		//scan.addColumn(family, qualifier)
		//scan.addFamily(family);
		//scan.setFilter(filter);
		//scan.setCacheBlocks(cacheBlocks);
		//scan.setCaching(caching);
		
		ResultScanner resultScanner=table.getScanner(scan);
		
		for(Result result:resultScanner){
			System.out.println(Bytes.toString(result.getRow()));
			System.out.println(result);
			System.out.println("=============================");
		}
		
		IOUtils.closeStream(resultScanner);
		IOUtils.closeStream(table);
	}
	
	public static void main(String[] args) throws IOException {
		putData();
	}
	
}
