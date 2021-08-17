package cn.rongcloud.moment.server.common.sql;

import com.alibaba.druid.pool.DruidDataSource;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class SqlConfiguration {

    private static Logger logger = LoggerFactory.getLogger(SqlConfiguration.class);

    @Value("${db.name}")
    private String dbName;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Value("${jdbc.driver}")
    private String jdbcDriver;

    @Value("${jdbc.username}")
    private String jdbcUsername;

    @Value("${jdbc.password}")
    private String jdbcPassword;

    @Value("${ds.initialSize}")
    private int dsInitialSize;

    @Value("${ds.minIdle}")
    private int dsMinIdle;

    @Value("${ds.maxActive}")
    private int dsMaxActive;

    @Value("${ds.maxWait}")
    private int dsMaxWait;

    @Value("${ds.timeBetweenEvictionRunsMillis}")
    private long dsTimeBetweenEvictionRunsMillis;

    @Value("${ds.minEvictableIdleTimeMillis}")
    private long dsMinEvictableIdleTimeMillis;

    @Bean(name = "dataSource", initMethod = "init", destroyMethod = "close")
    public DruidDataSource getDataSource() {
        try (DruidDataSource dataSource = new DruidDataSource()) {
            dataSource.setUrl(jdbcUrl);
            dataSource.setDriverClassName(jdbcDriver);
            dataSource.setUsername(jdbcUsername);
            dataSource.setPassword(jdbcPassword);

            //初始化大小、最小、最大
            dataSource.setInitialSize(dsInitialSize);
            dataSource.setMinIdle(dsMinIdle);
            dataSource.setMaxActive(dsMaxActive);

            //获取连接等待超时的时间
            dataSource.setMaxWait(dsMaxWait);

            //间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
            dataSource.setTimeBetweenEvictionRunsMillis(dsTimeBetweenEvictionRunsMillis);

            //一个连接在池中最小生存的时间，单位是毫秒
            dataSource.setMinEvictableIdleTimeMillis(dsMinEvictableIdleTimeMillis);


            if (Objects.equals(dbName, "gbase_oracle")){
                dataSource.setValidationQuery("SELECT current from dual");
            }else {
                dataSource.setValidationQuery("SELECT 'x'");
            }
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnBorrow(false);
            dataSource.setTestOnReturn(false);
            dataSource.setPoolPreparedStatements(false);
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
            dataSource.setFilters("stat");
            return dataSource;
        } catch (Exception e) {
            logger.error("Init DruidDataSource fail, exception: {}", e);
            return null;
        }
    }

    @Bean(name = "mybatisConfiguration")
    public org.apache.ibatis.session.Configuration getMybatisConfiguration() {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        //全局映射器启用缓存
        configuration.setCacheEnabled(true);
        //查询时，关闭关联对象即时加载以提高性能
        configuration.setLazyLoadingEnabled(true);
        //对于未知的SQL查询，允许返回不同的结果集以达到通用的效果
        configuration.setMultipleResultSetsEnabled(true);
        //允许使用列标签代替列名
        configuration.setUseColumnLabel(true);
        //不允许使用自定义的主键值(比如由程序生成的UUID 32位编码作为键值)，数据表的PK生成策略将被覆盖
        configuration.setUseGeneratedKeys(false);
        //给予被嵌套的resultMap以字段-属性的映射支持 FULL,PARTIAL
        configuration.setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
        //对于批量更新操作缓存SQL以提高性能 BATCH,SIMPLE
        configuration.setDefaultExecutorType(ExecutorType.BATCH);
        //数据库超过25000秒仍未响应则超时
//        configuration.setDefaultStatementTimeout(25000);
        //Allows using RowBounds on nested statements
        configuration.setSafeRowBoundsEnabled(false);
        //Enables automatic mapping from classic database column names A_COLUMN to camel case classic Java property names aColumn.
        configuration.setMapUnderscoreToCamelCase(true);
        //MyBatis uses local cache to prevent circular references and speed up repeated nested queries.By default (SESSION)all queries executed during a session are cached.If localCacheScope=STATEMENT local session will be used just for statement execution,no data will be shared between two different calls to the same SqlSession.
        configuration.setLocalCacheScope(LocalCacheScope.SESSION);
        //Specifies the JDBC type for null values when no specific JDBC type was provided for the parameter.Some drivers require specifying the column JDBC type but others work with generic values like NULL,VARCHAR or OTHER.
        configuration.setJdbcTypeForNull(JdbcType.OTHER);
        //Specifies which Object's methods trigger a lazy load
        configuration.setLazyLoadTriggerMethods(
            new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString")));
        //设置关联对象加载的形态，此处为按需加载字段(加载字段由SQL指 定)，不会加载关联表的所有字段，以提高性能
        configuration.setAggressiveLazyLoading(false);
        configuration.setLogImpl(Slf4jImpl.class);

        return configuration;
    }

    @Bean("sqlSessionFactory")
    public SqlSessionFactory getSqlSessionFactory(DataSource dataSource,
        org.apache.ibatis.session.Configuration mybatisConfiguration) {
        logger.info("sqlSessionFactory use: {}", dbName);
        return getRealSqlSessionFactory(dataSource, mybatisConfiguration,
            "classpath*:**/mapper/xml/" + dbName + "/*.xml");
    }

    private SqlSessionFactory getRealSqlSessionFactory(DataSource dataSource,
        org.apache.ibatis.session.Configuration mybatisConfiguration, String mapperLocation) {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfiguration(mybatisConfiguration);
        factoryBean.setTypeHandlersPackage("cn.rongcloud.moment.server.common.typehanlder");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            factoryBean.setMapperLocations(resolver.getResources(mapperLocation));
            return factoryBean.getObject();
        } catch (Exception e) {
            logger.error("getRealSqlSessionFactory exception: {}", e);
            return null;
        }
    }

    @EventListener
    public void onContextStopped(ContextStoppedEvent event) {
        logger.info("Shutdown db {} connection", dbName);
        if (dbName.equals("mysql")) {
            try {
                AbandonedConnectionCleanupThread.shutdown();
            } catch (Exception e) {
                logger.error("contextDestroyed AbandonedConnectionCleanupThread", e);
            }
        }
        Enumeration<Driver> driverEnumeration = DriverManager.getDrivers();
        while (driverEnumeration.hasMoreElements()) {
            Driver driver = driverEnumeration.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                logger.info("deregisterDriver {}", driver);
            } catch (Exception e) {
                logger.error("contextDestroyed deregisterDriver", e);
            }
        }
    }
}