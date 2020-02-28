package com.lagou.sqlSession;

import com.lagou.pojo.MappedStatement;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author: james.wu
 * @email: james.wu@nf-3.com
 * @date: 2020/2/27
 * @module: 类所属模块
 * @version: v1.0
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private final SqlSession sqlSession;


    private final Class<T> mapperInterface;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 准备参数 1：statmentid :sql语句的唯一标识：namespace.id= 接口全限定名.方法名
        // 方法名：findAll
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();
        String statementId = className+"."+methodName;
        MappedStatement mappedStatement = sqlSession.getConfiguration().getMappedStatementMap().get(statementId);
        if(mappedStatement == null) {
            throw new RuntimeException("不支持当前方法");
        }
        //判断sql类型
        switch (mappedStatement.getSqlCommandType()) {
            //查询语句
            case SELECT:
                // 准备参数2：params:args
                // 获取被调用方法的返回值类型
                Type genericReturnType = method.getGenericReturnType();
                // 判断是否进行了 泛型类型参数化
                if(genericReturnType instanceof ParameterizedType){
                    List<Object> objects = sqlSession.selectList(mappedStatement, args);
                    return objects;
                }
                return sqlSession.selectOne(mappedStatement,args);
            //插入语句
            case INSERT:
                return sqlSession.insert(mappedStatement, args);
            //更新语句
            case UPDATE:
                return sqlSession.update(mappedStatement, args);
            //删除语句
            case DELETE:
                return sqlSession.delete(mappedStatement, args);
            //未知语句，不支持
            case UNKNOWN:
            default:
                throw new RuntimeException("不支持当前语句");
        }
    }
}
