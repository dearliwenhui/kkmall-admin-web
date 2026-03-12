package com.ab.kkmalladminweb.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 *
 * @author KKMall
 * @since 2026-02-01
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器（分页插件）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(500L); // 设置单页最大限制数量
        paginationInnerInterceptor.setOverflow(false); // 溢出总页数后是否进行处理

        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }

    /**
     * 自动填充处理器
     * 用于自动填充 createTime 和 updateTime 字段
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 使用 setFieldValByName 代替 strictInsertFill，更可靠
                // 只在字段值为 null 时填充
                if (metaObject.hasGetter("createTime") && metaObject.getValue("createTime") == null) {
                    this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
                }
                if (metaObject.hasGetter("updateTime") && metaObject.getValue("updateTime") == null) {
                    this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时总是设置 updateTime
                if (metaObject.hasGetter("updateTime")) {
                    this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
                }
            }
        };
    }
}
