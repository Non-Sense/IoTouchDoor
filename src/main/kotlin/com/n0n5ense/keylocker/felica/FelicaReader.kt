package com.n0n5ense.keylocker.felica

import jp.shanimnni.Chipset
import jp.shanimnni.RCS380
import org.apache.commons.codec.binary.Hex
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.usb.UsbException

class FelicaReader() {

    private val reader:RCS380 = RCS380()

    private var executor:ExecutorService = Executors.newCachedThreadPool()

    var firmwareVersion:String? = null
        private set
    var pdDataVersion:String? = null
        private set
    val manufacturer = reader.manufacturer
    val productName = reader.productName

    private val interval:Long = 250

    var callback:((idm:Long,pmm:Long)->Unit)? = null

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
        reader.close()
        executor.shutdownNow()
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
                        callback?.invoke(
                                idm,
                                ByteBuffer.wrap(Arrays.copyOfRange(buf.array(), 15, 23)).long)
                    }
                    Thread.sleep(interval)
                }
            } catch (e:InterruptedException){
                close()
            } catch (e:UsbException){
                close()
            }
        }
    }
    fun isStopped():Boolean{
        return executor.isTerminated
    }
}