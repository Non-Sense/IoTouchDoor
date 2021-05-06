package com.n0n5ense.keylocker.service

import com.n0n5ense.keylocker.mapper.CardTouchLogMapper
import com.n0n5ense.keylocker.model.CardTouchLogModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class CardTouchLogService @Autowired constructor(private val dao: CardTouchLogMapper) {

    fun insert(log: CardTouchLogModel):Boolean{
        return dao.insert(log) > 0
    }

    fun selectAll():List<CardTouchLogModel>{
        return dao.selectAll()
    }
}