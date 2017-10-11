# SecKIll

本项目为高并发秒杀系统，以Spring、SpringMVC、Mybatis框架为核心实现，通过Redis缓存、存储过程对系统进行优化。

---

## 项目结构说明
seckill/

- dao：数据访问对象
- dao/cache：redis缓存逻辑
- dto：数据传输对象
- entity：实体类
- enums：常量枚举
- exception：自定义异常
- service：web业务层
- web：controller控制器

resources/

- 全局配置文件
- mapper：mybatis-mapper配置文件
- spring：spring配置文件及整合配置
- sql：数据库脚本

webapp/

- jsp：view页面
- script：前端脚本

--

## mybatis
第一部分通过mybatis对数据库操作

