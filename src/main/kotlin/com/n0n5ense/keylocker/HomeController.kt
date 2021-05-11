package com.n0n5ense.keylocker

import com.n0n5ense.keylocker.model.CardTouchLogModel
import com.n0n5ense.keylocker.model.UserModel
import com.n0n5ense.keylocker.service.CardTouchLogService
import org.springframework.beans.factory.annotation.Autowired
import com.n0n5ense.keylocker.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.web.bind.annotation.*

@RestController
class HomeController @Autowired constructor(val userService: UserService, val cardTouchLogService: CardTouchLogService, val felicaLogger: FelicaLogger) {

    @GetMapping("")
    fun index(): String = "Hello!"

    @RequestMapping("/start")
    fun start(): ResponseEntity<String>{
        return felicaLogger.init()
    }

    @RequestMapping("/status")
    fun status(): String{
        return felicaLogger.status()
    }

    @RequestMapping("/new")
    fun addUser(): ResponseEntity<UserModel>{
        val id = "1FEDCBA987654321"
        val name = "Unko"
        try {
            userService.insert(UserModel(null, name, id, true))
        } catch (e: UncategorizedSQLException){
            return ResponseEntity(null, HttpStatus.CONFLICT)
        }
        userService.select(id).let {
            if(it!=null)
                return ResponseEntity.ok(it)
            return ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping("/users")
    fun getUsers(): ResponseEntity<List<UserModel>>{
        return ResponseEntity.ok(userService.selectAll())
    }

    @RequestMapping("/logs")
    fun addTouchLog(): ResponseEntity<List<CardTouchLogModel>>{
        return ResponseEntity.ok(cardTouchLogService.selectAll())
    }
}