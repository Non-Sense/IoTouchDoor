package com.n0n5ense.keylocker.model

import java.time.ZonedDateTime

data class CardTouchLogModel(val id:Int?=null, val cardId:String, val time:ZonedDateTime, val accept: Boolean)
