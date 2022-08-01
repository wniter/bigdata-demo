项目描述：
有 5 种商品，每种商品的库存均是 10 件。当同时有上万客户秒杀商品时，
请合理设计。输出所有秒杀成功的客户信息。使用 Redis 和 Memcached 分别实现。
项目难点：
1、如何同时启动 1W 个线程(用来模拟买家)？ 2、如何保证修改商品数量操作的原子性？
3、如何保证不多卖？
解决方案： 1、用线程池先启动 1W 个线程，但是一进入 run 方法，立刻 sleep，约定好
一个时间，比如零点，计算当前线程开启时间与零点之间的差值作为线程睡
眠时间，到了零点线程自启，保证了 1W 个线程同时启动。
2、保证原子性
Redis:(利用监听、事务)
(1)获得 jedis 连接
(2)监听商品
(3)事务操作
Memcached:(利用 CAS 命令) 3、保证不多卖
Memcached 基本没有碰到这种情况(其实用 Memcached 做秒杀的本就不多)；
这里主要是 Redis：
第一步：获得商品数量
第二步：监听商品(watch)
第三步：再次获得商品数量，与第一步比较，数量有没有变化，变化了
的话立刻放弃监听(说明别的线程已经将数量改变)，没变的话进行第四步
第四步：开启事务(multi),商品数量减一
第五步：提交事务(exec)