package com.lagou.dao;

import com.lagou.pojo.User;

import java.util.List;

public interface IUserDao {

    //查询所有用户
    List<User> findAll() throws Exception;


    //根据条件进行用户查询
    User findByCondition(User user) throws Exception;

    /**
     * 插入数据
     * @param user
     * @return
     * @throws Exception
     */
    int insert(User user) throws Exception;

    /**
     * 更新数据
     * @param user
     * @return
     * @throws Exception
     */
    int update(User user) throws Exception;

    /**
     * 删除数据
     * @param user
     * @return
     * @throws Exception
     */
    int delete(User user) throws Exception;


}
