package com.yiworld.dao;

import com.yiworld.domain.LearnResouce;
import com.yiworld.tools.StringUtil;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Component注解不添加也没事，只是不加service那边引入LearnMapper会有错误提示，但不影响
 * 接口添加@Mapper注解或者在启动类上添加@MapperScan(“com.yiworld.dao”)注解都行
 */

/**
 * xml配置方式保持映射文件的老传统，优化主要体现在不需要实现dao的是实现层，
 * 系统会自动根据方法名在映射文件中找对应的sql
 * 只需新建LearnMapper接口，无需具体实现类，也不需任何注解 只需保留@Mapper注解
*/
@Component
@Mapper
public interface LearnMapper {
    @Insert("insert into learn_resource(author, title,url) values(#{author},#{title},#{url})")
    int add(LearnResouce learnResouce);

    @Update("update learn_resource set author=#{author},title=#{title},url=#{url} where id = #{id}")
    int update(LearnResouce learnResouce);

    @DeleteProvider(type = LearnSqlBuilder.class, method = "deleteByids")
    int deleteByIds(@Param("ids") String[] ids);


    @Select("select * from learn_resource where id = #{id}")
    @Results(id = "learnMap", value = {
            @Result(column = "id", property = "id", javaType = Long.class),
            @Result(property = "author", column = "author", javaType = String.class),
            @Result(property = "title", column = "title", javaType = String.class)
    })
    LearnResouce queryLearnResouceById(@Param("id") Long id);

    @SelectProvider(type = LearnSqlBuilder.class, method = "queryLearnResouceByParams")
    List<LearnResouce> queryLearnResouceList(Map<String, Object> params);

    class LearnSqlBuilder {
        public String queryLearnResouceByParams(final Map<String, Object> params) {
            StringBuffer sql =new StringBuffer();
            sql.append("select * from learn_resource where 1=1");
            if(!StringUtil.isNull((String)params.get("author"))){
                sql.append(" and author like '%").append((String)params.get("author")).append("%'");
            }
            if(!StringUtil.isNull((String)params.get("title"))){
                sql.append(" and title like '%").append((String)params.get("title")).append("%'");
            }
            System.out.println("查询sql=="+sql.toString());
            return sql.toString();
        }

        //删除的方法
        public String deleteByids(@Param("ids") final String[] ids){
            StringBuffer sql =new StringBuffer();
            sql.append("DELETE FROM learn_resource WHERE id in(");
            for (int i=0;i<ids.length;i++){
                if(i==ids.length-1){
                    sql.append(ids[i]);
                }else{
                    sql.append(ids[i]).append(",");
                }
            }
            sql.append(")");
            return sql.toString();
        }
    }
}
