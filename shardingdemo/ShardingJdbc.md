# 目录
[TOC]
# ShardingJdbc
`ShardingSphere` 是一套开源的分布式数据库中间件解决方案组成的生态圈，它由 JDBC（重点）、Proxy 和 Sidecar（规划中）这 3 款相互独立，却又能够混合部署配合使用的产品组成。 它们均提供标准化的数据分片、分布式事务和数据库治理功能，可适用于如 Java 同构、异构语言、云原生等各种多样化的应用场景。此篇文章主要是`ShardingJdbc`的环境搭建和基础分库分表的算法实现.   

## 核心概念  [目录](#目录) 
> 逻辑表：水平拆分的数据库的相同逻辑和数据结构表的总称  
> 真实表：在分片的数据库中真实存在的物理表。  
> 数据节点：数据分片的最小单元。由数据源名称和数据表组成  
> 绑定表：分片规则一致的主表和子表。  
> 广播表：也叫公共表，指素有的分片数据源中都存在的表，表结构和表中的数据
在每个数据库中都完全一致。例如字典表。  
> 分片键：用于分片的数据库字段，是将数据库(表)进行水平拆分的关键字段。
SQL中若没有分片字段，将会执行全路由，性能会很差。   
> 分片算法：通过分片算法将数据进行分片，支持通过=、BETWEEN和IN分片。
分片算法需要由应用开发者自行实现，可实现的灵活度非常高。   
> 分片策略：真正用于进行分片操作的是分片键+分片算法，也就是分片策略。在
ShardingJDBC中一般采用基于Groovy表达式的inline分片策略，通过一个包含
分片键的算法表达式来制定分片策略，如t_user_$->{u_id%8}标识根据u_id模
8，分成8张表，表名称为t_user_0到t_user_7。    

## 环境搭建 [目录](#目录)
环境：SpringBoot 2.3.1 + mybatis plus 3.0.5 + Sharding jdbc 4.1.1   

### 相关pom信息 [目录](#目录) 
```java
<modelVersion>4.0.0</modelVersion>

    <groupId>com.wl</groupId>
    <artifactId>shardingdemo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.3.1.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
            <version>4.1.1</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.1.22</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.0.5</version>
        </dependency>
    </dependencies>
```

### 项目目录结构 [目录](#目录)  
> adapter: 存放`controller`,主要是用于测试不同`分片策略`使用.   
> algo: 用于存放自定义的分片策略.     
> entity: 用于存放数据库实体的  
> mapper: 用于存放mybatis 的 mapper文件   

### 数据库信息 [目录](#目录)  
- 表结构创建语句
```sql
CREATE TABLE `wl_shard_test_1` (
  `cid` bigint(20) NOT NULL,
  `cname` varchar(50) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `cstatus` varchar(10) NOT NULL,
  PRIMARY KEY (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `wl_shard_test_2` (
  `cid` bigint(20) NOT NULL,
  `cname` varchar(50) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `cstatus` varchar(10) NOT NULL,
  PRIMARY KEY (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

- 数据库信息   
数据库目前使用的为: ==scrm-local== 和 ==jz_mall== 
具体使用的表名为: ==wl_shard_test_1== 和 ==wl_shard_test_2==    

### 基础类信息  
- 数据库对应的实体   
```java
@Data
@TableName("wl_shard_test")
public class WlShardTestEntity {

    private Long cid;

    private String cname;

    private Long userId;

    private String cstatus;

}
```

- mybatis 对应的mapper  
```java 
@Mapper
public interface WlShardTestMapper extends BaseMapper<WlShardTestEntity> {
} 
```

## 数据源配置  [目录](#目录) 

### 单库分表 [目录](#目录)
```
## 设置 shardingsphere 数据源 名称 多个以 逗号分隔 db0,db1
spring.shardingsphere.datasource.names=ds0

## 配置ds0数据源具体内容
spring.shardingsphere.datasource.ds0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=root
```

### 分库分表  [目录](#目录) 
```
## 设置 shardingsphere 数据源 名称 多个以 逗号分隔 db0,db1
spring.shardingsphere.datasource.names=ds0,ds1

## 配置ds0数据源具体内容
spring.shardingsphere.datasource.ds0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=root

## 配置ds1数据源具体内容
spring.shardingsphere.datasource.ds1.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://127.0.0.1:3306/test1?useUnicode=true&characterEncoding=UTF-8
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=root
```

## 分库分表策略  [目录](#目录)
Sharding jdbc为我们实际提供了5种的分库分表策略实现方式。如下：
> 标准分片策略 （PreciseShardingAlgorithm、RangeShardingAlgorithm）   
> 复合分片策略 （ComplexKeysShardingAlgorithm）   
> Hint分片策略  （HintShardingAlgorithm）  
> 行表达式分片策略   行表达式策略直接在配置文件中指定即可,不需要写对应的策略算法  
> 不分片策略    

### 行表达式分片策略  [目录](#目录)
#### 行表达式配置 [目录](#目录)
- 不分库,分表   
```
# 分表: 采用cid字段进行分表 
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.inline.sharding-column=cid
# 指定分片算法表达式, 表达式含义为: 根据cid取模 + 1, 就是数据所要存放的表
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.inline.algorithm-expression=wl_shard_test_$->{cid % 2 +1}
```
- 分库分表 
```
# 分库: 采用user_id字段进行分库
spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.inline.sharding-column=user_id
spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.inline.algorithm-expression=ds$->{user_id % 2}
# 多个库
# spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.inline.algorithm-expression=ds$->{0..1}.wl_shard_test_$->{1..2}

# 分表: 采用cid字段进行分表 
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.inline.sharding-column=cid
# 指定分片算法表达式, 表达式含义为: 根据cid取模 + 1, 就是数据所要存放的表
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.inline.algorithm-expression=wl_shard_test_$->{cid % 2 +1}
```
#### 行表达式含义  [目录](#目录)
数据库配置信息参照[数据源配置](#数据源配置).`database-strategy.inline`表示数据库使用`inline(行表达式策略)`.`table-strategy.inline`表示表使用`inline(行表达式策略)`.**inline的策略略默认不支持按分片键的范围查询**   
**ds$->{user_id % 2}** : 表示数据源选择`dsx`, x的取值为`user_id`取模的值,即数据源为`ds0,ds1`.   
**ds$->{0..1}.wl_shard_test_$->{1..2}** : 表示数据源是`ds0,ds1`,数据表是`wl_shard_test_1, wl_shard_test_2`.  
**wl_shard_test_$->{cid % 2 +1}** : 表示真实表为 `wl_shard_test_xx`, xx的取值为 `cid % 2 + 1`.因为真实表为`wl_shard_test_1,wl_shard_test_2,...`,所以采用了cid取模后加一,防止出现`wl_shard_test_0`的真实表.   

### 标准分片策略  [目录](#目录)  
标准分片策略可以同时实现 [精确分片算法](#精确分片算法) 和 [范围分片算法](#范围分片算法), [精确分片算法](#精确分片算法)是必须要有的,不然项目是无法启动的.   

#### 精确分片算法(PreciseShardingAlgorithm)    
精确分片算法，对应实现接口PreciseShardingAlgorithm。sql在分表键上执行 = 与 IN 时触发分表算逻辑，否则不走分表，全表执行;    

#### 范围分片算法(RangeShardingAlgorithm)   
sql在分表键上执行 BETWEEN AND、>、<、>=、<= 时触发分表算逻辑，否则不走分表，全表执行。  

#### 标准分片配置  [目录](#目录) 
```
## 使用精准的分片
### 库标准精确分片  根据user_id进行 分库 
spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.standard.sharding-column=user_id
### 精确分片 分库实现 
spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.standard.precise-algorithm-class-name=com.wl.shardingdemo.algo.MyDbPreciseShardingAlgorithm
### 范围分片 分库实现 
spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.standard.range-algorithm-class-name=com.wl.shardingdemo.algo.MyDbRangeShardingAlgorithm
#
#### 表标准精确分片 根据cid 进行分表 
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.standard.sharding-column=cid
### 精确分片 分表
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.standard.precise-algorithm-class-name=com.wl.shardingdemo.algo.MyTbPreciseShardingAlgorithm
### 范围分片 分表
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.standard.range-algorithm-class-name=com.wl.shardingdemo.algo.MyTbRangeShardingAlgorithm
```
#### 标准分片算法  [目录](#目录) 
##### PreciseShardingAlgorithm 实现  [目录](#目录)  
- 分库  
```java 
public class MyDbPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    /**
     * 精确分片算法
     *
     * @param dbNames           表信息
     * @param preciseShardingValue 分片键
     * @return 表名
     */
    @Override
    public String doSharding(Collection<String> dbNames, PreciseShardingValue<Long> preciseShardingValue) {
        String columnName = preciseShardingValue.getColumnName();
        if ("user_id".equals(columnName)) {
            Long value = preciseShardingValue.getValue();
            int size = dbNames.size();
            int dbSuffix = (int)(value % size + 1);
            Iterator<String> iterator = dbNames.iterator();
            int count = 1;
            // 根据 用户id奇偶 进行数据库路由
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (dbSuffix == count) {
                    return next;
                } else {
                    count++;
                }
            }
        }
        throw new RuntimeException("路由数据库不存在!");
    }
}
```

- 分表   
```
public class MyTbPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    /**
     * 精确分片算法
     *
     * @param tbNames           表信息
     * @param preciseShardingValue 分片键
     * @return 表名
     */
    @Override
    public String doSharding(Collection<String> tbNames, PreciseShardingValue<Long> preciseShardingValue) {
        String columnName = preciseShardingValue.getColumnName();
        if ("cid".equals(columnName)) {
            Long value = preciseShardingValue.getValue();
            int dbSuffix = (int)(value % 2 + 1);
            Iterator<String> iterator = tbNames.iterator();
            String logicTableName = preciseShardingValue.getLogicTableName();
            return logicTableName + "_" + dbSuffix;
        }
        throw new RuntimeException("路由数据库不存在!");
    }
}
```

##### RangeShardingAlgorithm 实现 [目录](#目录)   
- 分库  
```
public class MyDbRangeShardingAlgorithm implements RangeShardingAlgorithm<Integer> {

    /**
     * 范围库分片
     *
     * @param dbNames            库名
     * @param rangeShardingValue 值范围
     * @return 库名
     */
    @Override
    public Collection<String> doSharding(Collection<String> dbNames, RangeShardingValue<Integer> rangeShardingValue) {
        Range<Integer> valueRange = rangeShardingValue.getValueRange();
        Integer lowerValue = valueRange.lowerEndpoint();
        Integer upperValue = valueRange.upperEndpoint();
        Set<String> result = new LinkedHashSet<>();
        int size = dbNames.size();
        for (int i = lowerValue; i <= upperValue; i++) {
            for (String dbName : dbNames) {
                if (dbName.endsWith(i % size + "")) {
                    result.add(dbName);
                }
            }
        }
        return result;
    }
}
```

- 分表   
```
public class MyTbRangeShardingAlgorithm implements RangeShardingAlgorithm<Long> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        // 根据具体要求实现 
        String logicTableName = rangeShardingValue.getLogicTableName();
        return Arrays.asList("wl_shard_test_1","wl_shard_test_2");
    }

}
```

### 复合分片策略  [目录](#目录)   
复合分片策略用于处理使用多键（多字段）作为分片键进行分片的场景，包含多个分片键的逻辑较复杂，需要应用开发者自行处理其中的复杂度。需要配合ComplexShardingStrategy使用。复合分片策略提供对SQL语句中的 =, >, <, >=, <=, IN和BETWEEN AND 的分片操作支持。该策略实现了对多字段逻辑处理，以及返回多表的支持。  
#### 复杂分片策略配置 [目录](#目录)  
```
# 库复杂分片 字段
spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.complex.sharding-columns=user_id
# 库复杂分片 算法
spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.complex.algorithm-class-name=com.wl.shardingdemo.algo.MyDbComplexKeysShardingAlgorithm
# 表复杂分片字段
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.complex.sharding-columns=cid
# 表复杂分片算法
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.complex.algorithm-class-name=com.wl.shardingdemo.algo.MyTbComplexKeysShardingAlgorithm
```

#### ComplexKeysShardingAlgorithm实现  [目录](#目录)   
- 分库实现   
```java  
public class MyDbComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    /**
     * 复制分片 库分片算法
     *
     * @param dbNames                  库名
     * @param complexKeysShardingValue 分片键
     * @return 库名
     */
    @Override
    public Collection<String> doSharding(Collection<String> dbNames, ComplexKeysShardingValue<Long> complexKeysShardingValue) {
        Map<String, Collection<Long>> columnNameAndShardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();
        Set<String> result = new LinkedHashSet<>();
        if (columnNameAndShardingValuesMap.size() > 0) {
            Collection<Long> userIdColl = columnNameAndShardingValuesMap.get("user_id");
            if (userIdColl.size() > 0) {
                for (Long aLong : userIdColl) {
                    if (aLong % 2 == 0) {
                        result.add("ds0");
                    } else {
                        result.add("ds1");
                    }
                }
            }

        }
        Map<String, Range<Long>> columnNameAndRangeValuesMap = complexKeysShardingValue.getColumnNameAndRangeValuesMap();
        Range<Long> longRange = columnNameAndRangeValuesMap.get("user_id");
        if (null != longRange) {

            Long lowerEndpoint = longRange.lowerEndpoint();
            Long upperEndpoint = longRange.upperEndpoint();
            //  确定范围路由的表
            for (long i = lowerEndpoint; i <= upperEndpoint; i++) {
                result.add("ds" + (i % 2));
            }
        }

        return result;
    }
}
```

- 分表实现  
```java
public class MyTbComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    /**
     * 复制分片 库分片算法
     *
     * @param dbNames                  库名
     * @param complexKeysShardingValue 分片键
     * @return 库名
     */
    @Override
    public Collection<String> doSharding(Collection<String> dbNames, ComplexKeysShardingValue<Long> complexKeysShardingValue) {
        Map<String, Collection<Long>> columnNameAndShardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();
        Map<String, Range<Long>> columnNameAndRangeValuesMap = complexKeysShardingValue.getColumnNameAndRangeValuesMap();
        Collection<Long> cidCollection = columnNameAndShardingValuesMap.get("cid");
        String logicTableName = complexKeysShardingValue.getLogicTableName();
        Set<String> result = new LinkedHashSet<>();
        if (cidCollection.size() > 0) {
            for (Long next : cidCollection) {
                result.add(logicTableName + "_" + (next % 2 + 1));
            }
        }
        Range<Long> longRange = columnNameAndRangeValuesMap.get("cid");
        if (null != longRange) {

            Long lowerEndpoint = longRange.lowerEndpoint();
            Long upperEndpoint = longRange.upperEndpoint();
            //  确定范围路由的表
            for (long i = lowerEndpoint; i <= upperEndpoint; i++) {
                result.add(logicTableName + "_" + (i % 2 + 1));
            }
        }
        return result;
    }
}

```

### Hint分片策略 [目录](#目录) 
hint分片策略与其他分片策略不同，其他策略都是根据配置的分片键，以及配置的分片策略来实现表路由。当 hint用来实现比较复杂的sql或sql条件字段中没有分片字段时的一种强制路由策略。该策略需要在业务代码中使用HintManager 对象设置线程绑定参数，用于在该分片实现类中获取。使用完成之后不用再路由时，需要将该线程参数清除。   

#### Hint分片策略配置 [目录](#目录)  
```
spring.shardingsphere.sharding.tables.wl_shard_test.database-strategy.hint.algorithm-class-name=com.wl.shardingdemo.algo.MyDbHintShardingAlgorithm
spring.shardingsphere.sharding.tables.wl_shard_test.table-strategy.hint.algorithm-class-name=com.wl.shardingdemo.algo.MyTbHintShardingAlgorithm
```

#### HintShardingAlgorithm实现  [目录](#目录)   
- 库实现 
```java  
public class MyDbHintShardingAlgorithm implements HintShardingAlgorithm<Integer> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, HintShardingValue<Integer> hintShardingValue) {
        Collection<Integer> values = hintShardingValue.getValues();
        Set<String> set = new HashSet<>();
        for (Integer value : values) {
            set.add("ds" + value);
        }
        return set;
    }
}
```
- 表实现  
```java  
public class MyTbHintShardingAlgorithm implements HintShardingAlgorithm<Integer> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, HintShardingValue<Integer> hintShardingValue) {
        // 这里组装 表名即可
        String logicTableName = hintShardingValue.getLogicTableName();
        Collection<Integer> values = hintShardingValue.getValues();
        Set<String> set = new HashSet();
        for (Integer value : values) {
            set.add(logicTableName + "_" + value);
        }
        return set;
    }
}
```
- 调用代码    
```java  
@GetMapping("/hint/one")
public WlShardTestEntity getHintOne() {
    HintManager hintManager = HintManager.getInstance();
    // 控制路由到 具体的库 value值: 可以作为库的判断条件
    hintManager.addDatabaseShardingValue("wl_shard_test", 74%2);
    // 控制路由到 具体的表 value值: 可以作为表的判断条件
    hintManager.addTableShardingValue("wl_shard_test", 1);
    LambdaQueryWrapper<WlShardTestEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(WlShardTestEntity::getCid,683679938234023936L);
    wrapper.eq(WlShardTestEntity::getUserId,74L);
    WlShardTestEntity wlShardTestEntity = wlShardTestMapper.selectOne(wrapper);
    hintManager.close();
    return wlShardTestEntity;
}
```
## 其他配置  [目录](#目录)
```
## 指定cid的生成算法
### 指定cid为生成字段
spring.shardingsphere.sharding.tables.wl_shard_test.key-generator.column=cid
### 采用雪花算法
spring.shardingsphere.sharding.tables.wl_shard_test.key-generator.type=SNOWFLAKE  

## 打开 sharding sphere sql打印日志
spring.shardingsphere.props.sql.show=true
```
## 参考文档  [目录](#目录)
[shardingsphere官方文档](https://shardingsphere.apache.org/document/current/cn/features/sharding/use-norms/sql/)   
[shardingsphere csdn博客](https://blog.csdn.net/liuhenghui5201/category_10786898.html)





