package com.lagou.sqlSession;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;

import java.sql.SQLException;
import java.util.List;

public interface Executor {

    public <E> List<E> query(Configuration configuration,MappedStatement mappedStatement,Object... params) throws Exception;

    /**
     * 修改数据
     * @param ms
     * @param parameter
     * @return
     */
    int update(MappedStatement ms, Object... parameter) throws Exception;

}
