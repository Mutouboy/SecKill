package org.seckill.service.impl;


import org.apache.commons.collections4.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiangwenbin on 17/7/30.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    //注入service依赖
    @Autowired//@Resource,@inject都可以
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //用来混淆md5
    private final String slat = "dasgsgrbhdratw4t342314saf";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }


    /**
     * 最常见的方法：
     * get from cache
     * if null
     * get db
     * else
     * put cache
     * locgoin
     * <p>
     * 逻辑上没错，但直接写在service里不适合，应该放在dao里
     */
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：缓存优化
        //1:访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            //2:访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                //3:放入redis
                redisDao.putSeckill(seckill);
            }
        }
        Date stratTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();

        if (nowTime.getTime() < stratTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), stratTime.getTime(), endTime.getTime());
        }

        //转化特定字符串的过程
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }


    @Transactional
    /***
     * 使用注解控制事务方法的优点：
     * 1：开发团队达成一致约定，明确标注事务方法的编程风格
     * 2：保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，如：RPC/HTTP请求或者剥离到事物方法外部，再做一个更上层的方法
     * 3：不是所有的方法都需要事务，如只有一条修改操作、只读操作等不需要事务控制
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {

        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }

        /**
         * 原来执行的流程
         update(发送在mysql网络时间+gc时间） + insert(发送在mysql网络时间+gc时间)
         因为update同一行会导致行级锁，而insert是可以并行执行的。
         1.如果先update, update在前面会加锁
         锁 + update(发送在mysql网络时间+gc时间） + insert(发送在mysql网络时间+gc时间) + 提交锁
         其他的线程就要等，这个锁提交才能执行。
         2.如果先insert,
         insert(发送在mysql网络时间+gc时间） +  锁+ update(发送在mysql网络时间+gc时间) + 提交锁
         其他的线程可以并发insert. 这样子会减少锁的时长
         *
         * 没有优化之前的代码
         *
         * try {
         //执行秒杀逻辑：减库存，记录购买行为
         Date nowTime = new Date();
         int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
         if (updateCount<=0){
         //没有更新到记录,秒杀结束
         throw new SeckillCloseException("seckill is closed");
         }else {
         //成功,记录购买行为
         int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
         //唯一seckillId,userPhone
         if (insertCount<=0){
         //重复秒杀
         throw new RepeatKillException("seckill repeated");
         }else {
         //秒杀成功
         SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
         return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
         }
         }
         }catch (SeckillCloseException e1){
         //防止SeckillCloseException异常转化为Exception，提前捕获
         throw e1;
         }catch (RepeatKillException e2){
         throw e2;
         }catch(Exception e){
         logger.error(e.getMessage(),e);
         //所有编译期异常转化为运行期异常
         throw new SeckillException("seckill inner error"+e.getMessage());
         }
         */

        //优化之后
        try {
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一seckillId,userPhone
            if (insertCount <= 0) {
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                //执行秒杀逻辑：减库存，记录购买行为
                Date nowTime = new Date();
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新到记录,秒杀结束,rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    //秒杀成功commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            //防止SeckillCloseException异常转化为Exception，提前捕获
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所有编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error" + e.getMessage());
        }
    }


    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        try {
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1){
                SuccessKilled successKilled = successKilledDao.
                        queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,successKilled);
            }else {
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
