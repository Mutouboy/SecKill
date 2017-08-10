package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by jiangwenbin on 17/7/31.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})

public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {

        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}", list);//{}是占位符
    }

    @Test
    public void getById() throws Exception {

        long id = 1000l;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}", seckill);
    }

    @Test
    public void exportSeckillUrl() throws Exception {

        long id = 1000l;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer={}", exposer);

    }

    @Test
    public void executeSeckill() throws Exception {

        long id = 1000;
        long phone = 11111111111l;
        String md5 = "00f9ac36236aa9095b2cd0bd5aca0fdf";
        SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
        logger.info("result={}", seckillExecution);
    }

    @Test
    public void testSeckillLogic() {
        long id = 1001;
        long phone = 22222222222l;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()) {
            //如果秒杀开启
            try {
                String md5 = exposer.getMd5();
                SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
                logger.info("result={}", seckillExecution);
            } catch (RepeatKillException e1) {
                throw e1;
            } catch (SeckillCloseException e2) {
                throw e2;
            }

        } else {
            logger.warn("exposer={}", exposer);
        }

    }

    @Test
    public void testexecuteSeckillProcedure() {
        long id = 1000l;
        long phone = 15015015015l;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()) {
            //如果秒杀开启
            String md5 = exposer.getMd5();
            SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(id, phone, md5);
            logger.info(seckillExecution.getStateInfo());
        }
    }

}