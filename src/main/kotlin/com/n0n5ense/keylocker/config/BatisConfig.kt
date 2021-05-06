package com.n0n5ense.keylocker.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

@Configuration
class BatisConfig {
    @Autowired
    private lateinit var dataSourceProperties: DataSourceProperties

    @Bean(name=["dataSource"])
    fun dataSource():DataSource{
        val dataSource = SQLiteDataSource()
        dataSource.url = dataSourceProperties.url
        return dataSource
    }
}