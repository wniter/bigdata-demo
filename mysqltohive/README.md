问题：把 MySQL 中大量的表和数据迁移到 Hive 中
思路：
//1.把 MySQL 中所有的表的结构和数据找出来
//1.1 如何找出 MySQL 中的表
 ResultSet tables = DataBaseMetaData.getTables(null, null, null, new 
String[]{"TABLE"});
 //找到表的结构，字段名称、类型
 DataBaseMetaData.getColumns(....)
//1.2 导出 MySQL 中的数据
 先存放到 Linux 本地
 rs.getColumnCount()
 while(rs.next()){
 FileUtils.write(file, xxx, true);
 }
//2.在 Hive 中创建表、导入数据
//2.1 根据 1.1 中找到的 Mysql 表名称，在这里可以创建对应的 Hive 表
//2.2 导入 1.2 存放到本地磁盘的数据文件
 load data local inpath '....' into table ... 
 使用 hive -e '....'
//