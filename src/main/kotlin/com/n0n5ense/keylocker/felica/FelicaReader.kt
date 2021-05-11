package com.n0n5ense.keylocker.felica

import jp.shanimnni.Chipset
import jp.shanimnni.RCS380
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.usb.UsbException
import kotlin.collections.ArrayList

class FelicaReader {

    private val reader:RCS380 = RCS380()

    private var executor:ExecutorService = Executors.newCachedThreadPool()

    var firmwareVersion:String? = null
        private set
    private var pdDataVersion:String? = null
    val manufacturer:String = reader.manufacturer
    val productName:String = reader.productName

    private val interval:Long = 250

    private var callbacks:MutableList<((idm:String,pmm:String)->Unit)> = ArrayList()
    private var onCloses:MutableList<(()->Unit)> = ArrayList()

    fun addCallback(callback:(idm:String,pmm:String)->Unit){
        this.callbacks.add(callback)
    }
    fun removeAllCallbacks(){
        this.callbacks.clear()
    }
    fun addOnClose(callback:()->Unit){
        this.onCloses.add(callback)
    }
    fun removeAllOnCloses(){
        this.onCloses.clear()
    }

    fun openAndStart(){
        open()
        preCommand()
        start()
    }

    private fun open(){
        reader.open()
        executor.shutdownNow()
        executor = Executors.newCachedThreadPool()
    }

    fun close(){
        try {
            reader.close()
        } catch (e:Exception){
        } finally {
            executor.shutdownNow()
            onCloses.forEach { it() }
        }
    }

    private fun preCommand(){
        reader.sendCommand(Chipset.CMD_SET_COMMAND_TYPE, byteArrayOf(0x01))
        reader.sendCommand(Chipset.CMD_GET_FIRMWARE_VERSION).let {
            firmwareVersion = "%d.%02d".format(it.get(1), it.get(0))
        }
        reader.sendCommand(Chipset.CMD_GET_PD_DATA_VERSION).let {
            pdDataVersion = "%d.%02d".format(it.get(1), it.get(0))
        }
        reader.sendCommand(Chipset.CMD_SWITCH_RF, byteArrayOf(0x00))

        //0x01010f01 : F
        //0x02030f03 : A
        //0x03070f07 : B
        reader.sendCommand(Chipset.CMD_IN_SET_RF, byteArrayOf(0x01, 0x01, 0x0f, 0x01))
        reader.sendCommand(Chipset.CMD_IN_SET_PROTOCOL, byteArrayOf(0x00, 0x18, 0x01, 0x01, 0x02, 0x01, 0x03, 0x00, 0x04, 0x00, 0x05, 0x00, 0x06, 0x00, 0x07, 0x08, 0x08, 0x00, 0x09, 0x00, 0x0a, 0x00, 0x0b, 0x00, 0x0c, 0x00, 0x0e, 0x04, 0x0f, 0x00, 0x10, 0x00, 0x11, 0x00, 0x12, 0x00, 0x13, 0x06))
        reader.sendCommand(Chipset.CMD_IN_SET_PROTOCOL, byteArrayOf(0x00, 0x18))
    }

    private fun start(){
        executor.execute {
            try {
                var idm=0L
                while (true) {
                    val buf = reader.sendCommand(Chipset.CMD_IN_COMM_RF, byteArrayOf(0x6e, 0x00, 0x06, 0x00, 0xff.toByte(), 0xff.toByte(), 0x01, 0x00))
                    if (buf.array().contentEquals(byteArrayOf(0x80.toByte(), 0x00, 0x00, 0x00))) {
                        idm = 0L
                        continue
                    }
                    //Type-F
                    if (buf.get(5).toInt() == 0x14 && buf.get(6).toInt() == 0x01) {
                        val currentIdm=ByteBuffer.wrap(Arrays.copyOfRange(buf.array(), 7, 15)).long
                        if(currentIdm==idm)
                            continue
                        idm = currentIdm
                        val pmm = ByteBuffer.wrap(Arrays.copyOfRange(buf.array(), 15, 23)).long
                        callbacks.forEach {
                            it(String.format("%016X",idm), String.format("%016X",pmm))
                        }
                    }
                    Thread.sleep(interval)
                }
            } catch (e:InterruptedException){
                close()
            } catch (e:UsbException){
                close()
            } catch (e:Exception){
                close()
            }
        }
    }
    fun isStopped():Boolean{
        return executor.isTerminated
    }
}