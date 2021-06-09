package com.n0n5ense.keylocker.door


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


@Component
@ConfigurationProperties(prefix = "app")
class DoorConfig{
    var useDummyDoor:Boolean = false
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
        door = if(useDummyDoor)DummyDoor() else RealDoor()
        logger.info("useDummy: $useDummyDoor")
    }

    fun lock(){
        door?.lock()
    }
    fun unlock(){
        door?.unlock()
    }
    fun isLock():Boolean{
        return door?.isLock() == true
    }
    fun isClose():Boolean{
        return door?.isClose() == true
    }
}

private abstract class Door {

    companion object{
        private const val AUTO_LOCK_TIME: Long = 1000L*10L
        private const val LOCK_TIME: Long = 1000L*2L
        private const val POLLING_INTERVAL: Long = 500L
    }

    private var executor: ScheduledExecutorService? = null

    init {
        enableAutoLock()
    }

    abstract fun unlock()
    abstract fun lock()
    abstract fun isLock():Boolean
    abstract fun isClose():Boolean

    private fun polling():()->Unit {
        var beforeIsLock = true
        var beforeIsClose = true
        var lockOnLongClose = false
        var lastUnlockTime: Long
        var lastCloseTime: Long
        var openFlag = false

        System.currentTimeMillis().let {
            lastCloseTime = it
            lastUnlockTime = it
        }

        return {
            val isLock = isLock()
            val isClose = isClose()
            val currentTime = System.currentTimeMillis()
            Logger.getLogger("Door").info(
                "polling bl:$beforeIsLock bc:$beforeIsClose l:$isLock c:$isClose at:${currentTime-lastUnlockTime > AUTO_LOCK_TIME} lt:${currentTime-lastCloseTime > LOCK_TIME} op:$openFlag ll:$lockOnLongClose")
            // on unlock
            if (!isLock && beforeIsLock) {
                lastUnlockTime = currentTime
                openFlag = false
                lockOnLongClose = true
            }
            // on open
            if (!isClose && beforeIsClose) {
                lockOnLongClose = false
                openFlag = true
            }
            if (!isClose) {
                lastCloseTime = currentTime
            }
            if (isClose && lockOnLongClose && (currentTime - lastUnlockTime > AUTO_LOCK_TIME)) {
                lockOnLongClose = false
                lock()
            }
            if (!isLock && openFlag && (currentTime - lastCloseTime > LOCK_TIME)) {
                lock()
                openFlag = false
            }
            beforeIsLock = isLock
            beforeIsClose = isClose
        }
    }
    fun enableAutoLock(){
        if(isEnableAutoLock())
            return
        executor?.shutdownNow()
        executor = Executors.newSingleThreadScheduledExecutor()
        val f = polling()
        executor!!.scheduleAtFixedRate(f, 0L, POLLING_INTERVAL, TimeUnit.MILLISECONDS)
    }
    fun disableAutoLock(){
        executor?.shutdownNow()
    }
    fun isEnableAutoLock(): Boolean{
        return executor?.isShutdown == false
    }
}


private class DummyDoor: Door(){

    private var isLock = true
    private var isClose = true

    override fun unlock() {
        isLock = false
    }

    override fun lock() {
        if(!isClose)
            return
        isLock = true
    }

    override fun isLock():Boolean {
        return isLock
    }

    override fun isClose():Boolean {
        return isClose
    }
}

private class RealDoor: Door(){
    private var isLock = true
    private var isClose = true

    override fun unlock() {
        isLock = false
    }

    override fun lock() {
        if(!isClose())
            return
        isLock = true
    }

    override fun isLock():Boolean {
        return isLock
    }

    override fun isClose():Boolean {
        return isClose
    }
}