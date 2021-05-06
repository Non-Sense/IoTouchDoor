package com.n0n5ense.keylocker

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@MapperScan("com.n0n5ense.keylocker.mapper")
class KeylockerApplication

fun main(args: Array<String>) {
	println("started")
	runApplication<KeylockerApplication>(*args)
}
