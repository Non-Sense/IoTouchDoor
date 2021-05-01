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
    private var dataSourceProperties: DataSourceProperties? = null

    @Bean(name=["dataSource"])
    public fun dataSource():DataSource{
        val dataSource = SQLiteDataSource()
        dataSource.url = dataSourceProperties?.url
        return dataSource
    }
}