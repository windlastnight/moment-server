package cn.rongcloud.moment.server.common.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Use
 * @Project rce-server
 * @Author Created by CZN on 2017/5/26.
 */
public class ApplicationUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationUtil.applicationContext = applicationContext;
    }

    /**
     * 获取Spring上下文
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // 提供静态的方法获取bean对象
    public static <T> T getBean(String id, Class clazz) {
        return (T) applicationContext.getBean(id, clazz);
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public  static <T> T getBean(Class<T> t) {
        return applicationContext.getBean(t);
    }
}
