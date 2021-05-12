package com.n0n5ense.keylocker

import com.n0n5ense.keylocker.model.ColumnLimit
import com.n0n5ense.keylocker.model.CardTouchLogViewModel
import com.n0n5ense.keylocker.model.UserModel
import com.n0n5ense.keylocker.service.CardTouchLogService
import org.springframework.beans.factory.annotation.Autowired
import com.n0n5ense.keylocker.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.web.bind.annotation.*
import kotlin.math.min

@RestController
@RequestMapping("/api")
class RestApiController @Autowired constructor(val userService: UserService, val cardTouchLogService: CardTouchLogService, val felicaLogger: FelicaLogger) {

    @RequestMapping("/start")
    fun start(): ResponseEntity<String>{
        return felicaLogger.init()
    }

    @RequestMapping("/close")
    fun close(): ResponseEntity<Unit>{
        felicaLogger.close()
        return ResponseEntity(null,HttpStatus.NO_CONTENT)
    }

    @RequestMapping("/status")
    fun status(): String{
        return felicaLogger.status()
    }

    @PostMapping("/user")
    fun addUser(@RequestBody user: UserModel): ResponseEntity<UserModel>{
        if(user.cardId.length != 16)
            return ResponseEntity(null,HttpStatus.BAD_REQUEST)
        try {
            userService.insert(user)
        } catch (e: UncategorizedSQLException){
            return ResponseEntity(null, HttpStatus.CONFLICT)
        }
        userService.select(user.cardId).let {
            if(it!=null)
                return ResponseEntity.ok(it)
        }
        return ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PutMapping("/user")
    fun editUser(@RequestBody user: UserModel):ResponseEntity<UserModel>{
        userService.update(user).let {
            if(!it)
                return ResponseEntity(null,HttpStatus.BAD_REQUEST)
            return ResponseEntity.ok(user)
        }
    }

    @DeleteMapping("/user")
    fun deleteUser(@RequestBody user: UserModel):ResponseEntity<Unit>{
        userService.delete(user).let {
            if(!it)
                return ResponseEntity(null,HttpStatus.NOT_FOUND)
            return ResponseEntity(null,HttpStatus.NO_CONTENT)
        }
    }

    @GetMapping("/user")
    fun findUser(@RequestParam(name="q",required = true)id:String): ResponseEntity<UserModel>{
        if(id.length != 16)
            return ResponseEntity(null, HttpStatus.NOT_FOUND)
        val user = userService.select(id)
        if(user==null)
            return ResponseEntity(null, HttpStatus.NOT_FOUND)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/users")
    fun getUsers(): ResponseEntity<List<UserModel>>{
        return ResponseEntity.ok(userService.selectAll())
    }

    @GetMapping("/logs")
    fun addTouchLog(@RequestParam(name="l",required=false) limit: Int?, @RequestParam(name="o",required=false) offset:Int?): ResponseEntity<List<CardTouchLogViewModel>>{
        val l = min(limit?:30,100)
        return ResponseEntity.ok(cardTouchLogService.selectAll(ColumnLimit(l,offset?:0)))
    }
}