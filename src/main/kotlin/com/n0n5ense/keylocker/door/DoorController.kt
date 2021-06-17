package com.n0n5ense.keylocker.door


import com.n0n5ense.keylocker.model.PhysicalLogModel
import com.n0n5ense.keylocker.service.PhysicalLogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.logging.Logger


@Component
@ConfigurationProperties(prefix = "door")
class DoorConfig{
    var useDummyDoor:Boolean = false
    var host = ""
    var port = 0
}

@Component
class DoorController @Autowired constructor(private val doorConfig: DoorConfig, private val physicalLogService: PhysicalLogService) {

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

        door?.onClose = {
            physicalLogService.insert(PhysicalLogModel(action = "Close", time = ZonedDateTime.now()))
        }
        door?.onOpen = {
            physicalLogService.insert(PhysicalLogModel(action = "Open", time = ZonedDateTime.now()))
        }
        door?.onLock = {
            physicalLogService.insert(PhysicalLogModel(action = "Lock", time = ZonedDateTime.now()))
        }
        door?.onUnlock = {
            physicalLogService.insert(PhysicalLogModel(action = "Unlock", time = ZonedDateTime.now()))
        }
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