package org.pagebyfeel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    
    /**
     * String-String 전용 RedisTemplate
     * JWT 토큰, 블랙리스트 등 간단한 문자열 저장용
     */
    @Bean(name = "customStringRedisTemplate")
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // 모든 직렬화를 String으로 설정
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 범용 RedisTemplate (String-Object)
     * - Key: String 직렬화
     * - Value: JSON 직렬화 (Java 8 날짜/시간 API 지원)
     * - Hash Key: String 직렬화
     * - Hash Value: JSON 직렬화
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // ObjectMapper 설정 (LocalDateTime 등 Java 8 시간 API 지원)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // Key-Value 직렬화 설정
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        
        // Hash Key-Value 직렬화 설정
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(serializer);
        
        // 트랜잭션 지원 활성화 (필요시)
        template.setEnableTransactionSupport(false);
        
        template.afterPropertiesSet();
        return template;
    }
}
