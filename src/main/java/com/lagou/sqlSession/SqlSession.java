package com.lagou.sqlSession;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;

import java.sql.SQLException;
import java.util.List;

public interface SqlSession {

    /**
     * 插入数据
     * @param statement 插入语句id
     * @param parameter 参数
     * @return
     */
    int insert(String statement, Object... parameter) throws Exception;

    /**
     * 插入数据
     * @param statement
     * @param parameter
     * @return
     * @throws SQLException
     */
    int insert(MappedStatement statement, Object... parameter) throws Exception;

    /**
     * 更新数据
     * @param statement 更新语句id
     * @param parameter 参数
     * @return
     */
    int update(String statement, Object... parameter) throws Exception;

    /**
     * 更新数据
     * @param statement 更新语句statement
     * @param parameter 参数
     * @return
     */
    int update(MappedStatement statement, Object... parameter) throws Exception;

    /**
     * 删除数据
     * @param statement 更新语句id
     * @param parameter 参数
     * @return
     */
    int delete(String statement, Object... parameter) throws Exception;

    /**
     * 删除数据
     * @param statement 更新语句statement
     * @param parameter 参数
     * @return
     */
    int delete(MappedStatement statement, Object... parameter) throws Exception;

    /**
     * 查询所有
     * @param statementid
     * @param params
     * @param <E>
     * @return
     * @throws Exception
     */
    <E> List<E> selectList(String statementid,Object... params) throws Exception;

    /**
     * 查询所有
     * @param statementid
     * @param params
     * @param <E>
     * @return
     * @throws Exception
     */
    <E> List<E> selectList(MappedStatement statementid,Object... params) throws Exception;

    /**
     * 根据条件查询单个
     * @param statementid
     * @param params
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T selectOne(String statementid,Object... params) throws Exception;

    /**
     * 根据条件查询单个
     * @param statement
     * @param params
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T selectOne(MappedStatement statement,Object... params) throws Exception;


    /**
     * 为Dao接口生成代理实现类
     * @param mapperClass
     * @param <T>
     * @return
     */
    <T> T getMapper(Class<?> mapperClass);

    /**
     * 获取configuration
     * @return
     */
    Configuration getConfiguration();


}
