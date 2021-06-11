package com.n0n5ense.keylocker.felica

import com.n0n5ense.keylocker.door.DoorController
import com.n0n5ense.keylocker.model.CardTouchLogModel
import com.n0n5ense.keylocker.service.CardTouchLogService
import com.n0n5ense.keylocker.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.logging.Logger
import javax.annotation.PreDestroy

@Component
class FelicaLogger @Autowired constructor(
        val userService: UserService,
        val cardTouchLogService: CardTouchLogService,
        val doorController: DoorController):ApplicationListener<ContextClosedEvent>{
    var reader:FelicaReader? = null

    init {
        Runtime.getRuntime().addShutdownHook(Thread{
            this.close()
        })
    }

    fun init(): ResponseEntity<String>{
        if(reader!=null)
            return ResponseEntity("""{"result":"already started"}""", HttpStatus.CONFLICT)
        try {
            reader = FelicaReader().apply {
                addCallback { idm, _ -> touch(idm) }
                addOnClose { reader = null }
            }
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

    @PreDestroy
    fun close(){
        Logger.getLogger("FelicaLogger").info("close")
        reader?.close()
        reader = null
    }

    fun status():String{
        val device:String? = if(reader==null)
            null
        else
            """"${reader?.manufacturer} ${reader?.productName} ${reader?.firmwareVersion}""""
        val connected:Boolean = reader != null
        return """{"connected":$connected, "device":$device}"""
    }

    private fun touch(idm:String){
        Logger.getLogger("cardTouch").info(idm)
        var accept = false
        userService.select(idm).let {
            if(it?.enabled == true)
                accept = true
        }
        if(accept){
            doorController.unlock()
        }
        cardTouchLogService.insert(CardTouchLogModel(cardId = idm, time = ZonedDateTime.now(),accept = accept))
    }

    @EventListener
    override fun onApplicationEvent(event: ContextClosedEvent) {
        close()
    }
}