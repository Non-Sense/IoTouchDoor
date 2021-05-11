package com.n0n5ense.keylocker

import com.n0n5ense.keylocker.felica.FelicaReader
import com.n0n5ense.keylocker.model.CardTouchLogModel
import com.n0n5ense.keylocker.service.CardTouchLogService
import com.n0n5ense.keylocker.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.logging.Logger

@Component
class FelicaLogger @Autowired constructor(val userService: UserService, val cardTouchLogService: CardTouchLogService){
    var reader:FelicaReader? = null

    fun init(): ResponseEntity<String>{
        if(reader!=null)
            return ResponseEntity("""{"result":"already started"}""", HttpStatus.CONFLICT)
        reader = FelicaReader().apply {
            addCallback { idm, _ -> touch(idm) }
            addOnClose { reader = null }
        }
        try {
            reader?.openAndStart()
        } catch (e:Exception){
            try{
                reader?.close()
            } catch (e:Exception){}
            reader = null
            return ResponseEntity("""{"result":"failed"}""", HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity("""{"result":"successful"}""", HttpStatus.OK)
    }

    fun close(){
        reader?.close()
        reader = null
    }

    fun status():String{
        val device:String? = if(reader==null)
            null
        else
            """"${reader?.manufacturer} ${reader?.firmwareVersion}""""
        val connected:Boolean = reader != null
        return """{"connected":$connected, "Device":$device}"""
    }

    private fun touch(idm:String){
        Logger.getLogger("cardTouch").info(idm)
        var accept = false
        userService.select(idm).let {
            if(it?.enabled == true)
                accept = true
        }
        cardTouchLogService.insert(CardTouchLogModel(cardId = idm, time = ZonedDateTime.now(),accept = accept))
    }
}