package com.lagou.test;

import com.lagou.dao.IUserDao;
import com.lagou.io.Resources;
import com.lagou.pojo.User;
import com.lagou.sqlSession.SqlSession;
import com.lagou.sqlSession.SqlSessionFactory;
import com.lagou.sqlSession.SqlSessionFactoryBuilder;
import org.dom4j.DocumentException;
import org.junit.*;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.util.List;

public class IPersistenceTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void after() throws PropertyVetoException, DocumentException {
        InputStream resourceAsSteam = Resources.getResourceAsSteam("sqlMapConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsSteam);
    }

    @Test
    public void testInsert() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        User user = new User();
        user.setUsername("沙增");
        int result = sqlSession.insert(IUserDao.class.getName() + ".insert", user);
        System.out.println(result);
    }

    @Test
    public void testInsertByMapper() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        User user = new User();
        user.setUsername("白龙马");
        int result = userDao.insert(user);
        System.out.println(result);
    }

    @Test
    public void testUpdate() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        User user = new User();
        user.setId(6);
        user.setUsername("沙和尚");
        int result = sqlSession.update(IUserDao.class.getName() + ".update", user);
        System.out.println(result);
    }

    @Test
    public void testUpdateByMapper() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        User user = new User();
        user.setId(6);
        user.setUsername("卷帘大将");
        int result = userDao.update(user);
        System.out.println(result);
    }

    @Test
    public void testDelete() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        User user = new User();
        user.setId(5);
        int result = sqlSession.update(IUserDao.class.getName() + ".delete", user);
        System.out.println(result);
    }

    @Test
    public void testDeleteByMapper() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        User user = new User();
        user.setId(7);
        int result = userDao.delete(user);
        System.out.println(result);
    }

    @Test
    public void test() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        //调用
        User user = new User();
        user.setId(1);
        user.setUsername("孙悟空");
        User user2 = sqlSession.selectOne(IUserDao.class.getName() + ".findByCondition", user);

        System.out.println(user2);

       List<User> users = sqlSession.selectList(IUserDao.class.getName() + ".findAll");
        for (User user1 : users) {
            System.out.println(user1);
        }

        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        List<User> all = userDao.findAll();
        for (User user1 : all) {
            System.out.println(user1);
        }


    }



}
