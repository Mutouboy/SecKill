package org.seckill.dao;

import com.sun.org.glassfish.gmbal.ParameterNames;
import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jiangwenbin on 17/7/28.
 */

public interface SeckillDao {


    /***
     * 减库存
     * @param seckillId
     * @param killTime
     * @return 如果影响行数>1，表示更新的记录的行数
     */
    int reduceNumber(@Param("seckillId")long seckillId, @Param("killTime")Date killTime);


    /***
     * 根据秒杀ID查询秒杀对象
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);


    /***
     *根据偏移量查询秒杀商品列表
     * 在java中，形参(int offset,int limit)会被转化为(arg0,arg1)
     * 参数名不会被保存，为了区分复数个参数的名字，需要添加@Param注解来标识
     * @param offset
     * @param limit
     * @return
     */
    List<Seckill> queryAll(@Param("offset") int offset,@Param("limit") int limit);



    /**
     * 使用存储过程执行秒杀
     * @param paramMap
     */
    void killByProcedure(Map<String,Object> paramMap);

}
