package com.n0n5ense.keylocker.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PhysicalLogModel(val id:Int?=null, val action:String, time: ZonedDateTime){
    val time:String = time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    constructor(id:Int?=null, action: String, time: String) :
            this(id, action,ZonedDateTime.parse(time))
}
