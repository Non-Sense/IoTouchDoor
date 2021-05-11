package com.n0n5ense.keylocker.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CardTouchLogModel(val id:Int?=null, val cardId:String, time:ZonedDateTime, val accept: Boolean){
    val time:String = time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    constructor(id:Int?=null, cardId: String, time: String, accept: Boolean) :
            this(id,cardId,ZonedDateTime.parse(time),accept)
}
