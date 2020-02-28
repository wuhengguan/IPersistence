package com.lagou.sqlSession;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    simpleExecutor simpleExecutor;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
        simpleExecutor = new simpleExecutor(configuration);
    }

    @Override
    public int insert(String statement, Object... parameter) throws Exception {
        return update(statement, parameter);
    }

    @Override
    public int insert(MappedStatement statement, Object... parameter) throws Exception {
        return update(statement, parameter);
    }

    @Override
    public int update(String statement, Object... parameter) throws Exception {
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statement);
        return update(mappedStatement, parameter);
    }

    @Override
    public int update(MappedStatement statement, Object... parameter) throws Exception {
        if(statement == null) {
            throw new RuntimeException("不存在当前方法");
        }
        return simpleExecutor.update(statement, parameter);
    }

    @Override
    public int delete(String statement, Object... parameter) throws Exception {
        return update(statement, parameter);
    }

    @Override
    public int delete(MappedStatement statement, Object... parameter) throws Exception {
        return update(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statementid, Object... params) throws Exception {
        //将要去完成对simpleExecutor里的query方法的调用
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementid);
        return selectList(mappedStatement, params);
    }

    @Override
    public <E> List<E> selectList(MappedStatement statementid, Object... params) throws Exception {
        List<Object> list = simpleExecutor.query(configuration, statementid, params);
        return (List<E>) list;
    }

    @Override
    public <T> T selectOne(String statementid, Object... params) throws Exception {
        //将要去完成对simpleExecutor里的query方法的调用
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementid);
        return selectOne(mappedStatement, params);
    }

    @Override
    public <T> T selectOne(MappedStatement statement, Object... params) throws Exception {
        if(statement == null) {
            throw new RuntimeException("不存在当前方法");
        }
        List<Object> objects = selectList(statement, params);
        if(objects.size()==1){
            return (T) objects.get(0);
        }else {
            throw new RuntimeException("查询结果为空或者返回结果过多");
        }
    }

    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        // 使用JDK动态代理来为Dao接口生成代理对象，并返回

        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(),
                new Class[]{mapperClass}, new MapperProxy<T>(this, (Class<T>) mapperClass));

        return (T) proxyInstance;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
