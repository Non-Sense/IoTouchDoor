package com.n0n5ense.keylocker.config

import org.mybatis.spring.mapper.MapperScannerConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BatisMapperScannerConfig {
    @Bean
    fun mapperScannerConfigurer():MapperScannerConfigurer{
        val mapperScannerConfigurer = MapperScannerConfigurer()
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory")
        mapperScannerConfigurer.setBasePackage("com.n0n5ense.keylocker.mapper")
        return mapperScannerConfigurer
    }
}