package com.n0n5ense.keylocker

import com.n0n5ense.keylocker.felica.FelicaReader
import com.n0n5ense.keylocker.model.CardTouchLogModel
import com.n0n5ense.keylocker.model.UserModel
import com.n0n5ense.keylocker.service.CardTouchLogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.n0n5ense.keylocker.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.UncategorizedSQLException
import java.time.ZonedDateTime
import java.util.logging.Logger

@RestController
class HomeController @Autowired constructor(val userService: UserService, val cardTouchLogService: CardTouchLogService) {

    var reader: FelicaReader? = null

    @GetMapping("")
    fun index(): String = "Hello!"

    @RequestMapping("/start")
    fun start(model: Model): ResponseEntity<String>{
        if(reader!=null)
            return ResponseEntity("already started",HttpStatus.CONFLICT)
        try {
            reader = FelicaReader().apply {
                addCallback { idm, _ -> touch(idm) }
                addOnClose { reader = null }
                openAndStart()
            }
        } catch (e:Exception){
            try{
                reader?.close()
            } catch (e:Exception){
            }
            reader = null
            return ResponseEntity("failed", HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity("success", HttpStatus.OK)
    }

    private fun touch(idm:String){
        Logger.getLogger("cardTouch").info(idm)
        var accept = false
        userService.select(idm).let {
            if(it?.valid == true)
                accept = true
        }
        cardTouchLogService.insert(CardTouchLogModel(cardId = idm, time = ZonedDateTime.now(),accept = accept))
    }

    @RequestMapping("/status")
    fun status(model: Model): String{
        val device:String? = if(reader==null)
            null
        else
            """"${reader?.manufacturer} ${reader?.firmwareVersion}""""
        val connected:Boolean = reader != null
        return """{"connected":$connected, "Device":$device}"""
    }

    @RequestMapping("/new")
    fun addUser(model: Model): ResponseEntity<UserModel>{
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
    fun getUsers(model:Model): ResponseEntity<List<UserModel>>{
        return ResponseEntity.ok(userService.selectAll())
    }

    @RequestMapping("/logging")
    fun addTouchLog(model: Model): String{
        val t=cardTouchLogService.insert(CardTouchLogModel(null,"1234", ZonedDateTime.now(),false))
        return "success $t  $model"
    }
}