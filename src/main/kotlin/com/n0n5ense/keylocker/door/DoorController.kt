package com.n0n5ense.keylocker.door


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.logging.Logger


@Component
@ConfigurationProperties(prefix = "door")
class DoorConfig{
    var useDummyDoor:Boolean = false
    var host = ""
    var port = 0
}

@Component
class DoorController @Autowired constructor(private val doorConfig: DoorConfig) {

    var useDummyDoor:Boolean = doorConfig.useDummyDoor
    private var door: Door? = null

    private val logger = Logger.getLogger("Door")

    init {
        init()
    }

    private fun init(){
        if(door!=null)
            return
        door = if(useDummyDoor)DummyDoor() else RealDoor(doorConfig.host,doorConfig.port)
        logger.info("useDummy: $useDummyDoor")
    }

    fun lock(){
        door?.lock()
    }
    fun unlock(){
        door?.unlock()
    }
    fun getStatus(): DoorStatus?{
        return door?.getStatus()
    }
}