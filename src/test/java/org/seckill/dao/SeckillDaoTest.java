package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by jiangwenbin on 17/7/29.
 * 配置spring和junit整合，这样junit在启动时候就会加载spring容器
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {
//    注入Dao实现类依赖
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void reduceNumber() throws Exception {

        Date killdate = new Date();
       int state = seckillDao.reduceNumber(1000l,killdate);
       System.out.println("state: "+state);
    }

    @Test
    public void queryById() throws Exception {

        long seckilId=1000;
        Seckill seckill = seckillDao.queryById(seckilId);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }


    /***
     * 查询100条信息，从0开始
     * @throws Exception
     */
    @Test
    public void queryAll() throws Exception {

        List<Seckill> list = seckillDao.queryAll(0,100);
        for (Seckill s:list) {
            System.out.println(s);
        }
    }

}