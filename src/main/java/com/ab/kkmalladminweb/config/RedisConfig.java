package com.ab.kkmalladminweb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 *
 * @author KKMall
 * @since 2026-02-01
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.key-prefix:}")
    private String keyPrefix;

    /**
     * 配置 RedisTemplate
     * 使用 Jackson 序列化器，将对象序列化为 JSON
     * 支持环境隔离的 key 前缀
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用带前缀的 String 序列化器作为 key 的序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 如果配置了 key 前缀，使用自定义序列化器
        if (keyPrefix != null && !keyPrefix.isEmpty()) {
            template.setKeySerializer(new PrefixStringRedisSerializer(keyPrefix));
            template.setHashKeySerializer(stringRedisSerializer);
        } else {
            template.setKeySerializer(stringRedisSerializer);
            template.setHashKeySerializer(stringRedisSerializer);
        }

        // 使用 Jackson 序列化器作为 value 的序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 自定义 Redis Key 序列化器，自动添加环境前缀
     */
    private static class PrefixStringRedisSerializer extends StringRedisSerializer {
        private final String prefix;

        public PrefixStringRedisSerializer(String prefix) {
            this.prefix = prefix.endsWith(":") ? prefix : prefix + ":";
        }

        @Override
        public byte[] serialize(String string) {
            if (string == null) {
                return super.serialize(null);
            }
            // 自动添加前缀
            return super.serialize(prefix + string);
        }

        @Override
        public String deserialize(byte[] bytes) {
            String key = super.deserialize(bytes);
            if (key != null && key.startsWith(prefix)) {
                // 反序列化时移除前缀
                return key.substring(prefix.length());
            }
            return key;
        }
    }
}
