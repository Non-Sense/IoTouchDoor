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
import java.time.ZonedDateTime

@RestController
class HomeController @Autowired constructor(val userService: UserService, val cardTouchLogService: CardTouchLogService) {

    var reader: FelicaReader? = null

    @GetMapping("")
    fun index(): String = "Hello!"

    @RequestMapping("/start")
    fun start(model: Model):String{
        if(reader!=null)
            return "already started"
        try {
            reader = FelicaReader().apply {
                callback = { idm, _ ->
                    cardTouchLogService.insert(CardTouchLogModel(null,idm, ZonedDateTime.now()))
                }
                openAndStart()
            }
        } catch (e:Exception){
            try{
                reader?.close()
            } catch (e:Exception){
            }
            reader = null
            return "failed"
        }
        return "success"
    }

    @RequestMapping("/status")
    fun status(model: Model): String{
        return " ${reader?.manufacturer}\n${reader?.firmwareVersion} "
    }

    @RequestMapping("/new")
    fun addUser(model: Model): String{
        //val t = model.addAttribute("User", UserModel(null,"Unko","KOSJDFKLJA"))
        if(userService.select(0xfedcba987654321L)!=null){
            return "already exists"
        }
        val t=userService.insert(UserModel(null,"Unko",0xfedcba987654321L))
        return "success $t"
    }

    @RequestMapping("/logging")
    fun addTouchLog(model: Model): String{
        val t=cardTouchLogService.insert(CardTouchLogModel(null,1234L, ZonedDateTime.now()))
        //val t=userService.insert(UserModel(null,"Unko",0xfedcba987654321L))
        return "success $t  $model"
    }
}