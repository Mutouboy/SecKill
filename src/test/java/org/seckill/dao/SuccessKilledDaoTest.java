package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Created by jiangwenbin on 17/7/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() throws Exception {

     int count= successKilledDao.insertSuccessKilled(1001l,13216813816l);
        System.out.println(count);
    }

    @Test
    public void queryByIdWithSeckill() throws Exception {

       SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(1001l,13216813816l);
        System.out.println(successKilled);

    }

}