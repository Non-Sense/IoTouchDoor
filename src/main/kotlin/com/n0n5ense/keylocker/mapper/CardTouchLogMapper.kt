package com.n0n5ense.keylocker.mapper

import com.n0n5ense.keylocker.model.CardTouchLogModel
import com.n0n5ense.keylocker.model.ColumnLimit
import com.n0n5ense.keylocker.model.CardTouchLogViewModel
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface CardTouchLogMapper {
    @Insert("INSERT INTO touchLog(card_id, time, accept) VALUES(#{cardId}, #{time}, #{accept})")
    fun insert(model: CardTouchLogModel): Int

    @Select("SELECT touchLog.id,touchLog.card_id,time,accept,name FROM touchLog LEFT OUTER JOIN user ON touchLog.card_id=user.card_id ORDER BY time DESC LIMIT #{limit} OFFSET #{offset} ")
    fun selectAll(limit:ColumnLimit): List<CardTouchLogViewModel>
}