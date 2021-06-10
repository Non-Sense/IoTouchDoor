package com.n0n5ense.keylocker.door

import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

data class DoorStatus(val isLock:Boolean, val isClose:Boolean)

abstract class Door {

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
    abstract fun getStatus(): DoorStatus?

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
            val status = getStatus()
            val isLock = status?.isLock == true
            val isClose = status?.isClose == true
            val currentTime = System.currentTimeMillis()
//            Logger.getLogger("Door").info(
//                "polling bl:$beforeIsLock bc:$beforeIsClose l:$isLock c:$isClose at:${currentTime-lastUnlockTime > AUTO_LOCK_TIME} lt:${currentTime-lastCloseTime > LOCK_TIME} op:$openFlag ll:$lockOnLongClose")
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


class DummyDoor: Door(){

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

    override fun getStatus(): DoorStatus {
        return DoorStatus(isLock,isClose)
    }
}

class RealDoor(private val host:String, private val port:Int): Door(){
    private var socket:Socket? = null
    private var reader:DataInputStream? = null
    private var writer:DataOutputStream? = null

    private fun connect(){
        try {
            socket = Socket(host, port)
        } catch (e:IOException){
            Logger.getLogger("Door").warning("ConnectError")
            disconnect()
            return
        }
        reader = DataInputStream(socket!!.getInputStream())
        writer = DataOutputStream(socket!!.getOutputStream())
    }

    private fun disconnect(){
        socket?.close()
        reader = null
        writer = null
        socket = null
    }

    enum class Command(val v:Int){
        UNLOCK(1),
        LOCK(2),
        STATUS(3),
    }

    override fun unlock() {
        Logger.getLogger("Door").info("unlock")
        try {
            connect()
            writer?.write(Command.UNLOCK.v)
        } catch (e:IOException){
            Logger.getLogger("Door").warning("IOException: unlock")
        }
        disconnect()
    }

    override fun lock() {
        Logger.getLogger("Door").info("lock")
        if(getStatus()?.isLock == true)
            return
        try{
            connect()
            writer?.write(Command.LOCK.v)
        } catch (e:IOException){
            Logger.getLogger("Door").warning("IOException: lock")
        }
        disconnect()
    }

    override fun getStatus(): DoorStatus? {
        try {
            connect()
            writer?.write(Command.STATUS.v)
            val state = reader?.read()
            disconnect()
            state?:return null
            //Logger.getLogger("Door").info("st: $state l:${(state and 1) != 0} c:${(state and 2) != 0}")
            return DoorStatus((state and 1) != 0, (state and 2) != 0)
        } catch (e:IOException){
            Logger.getLogger("Door").warning("IOException: status")
            disconnect()
            return null
        }
    }
}